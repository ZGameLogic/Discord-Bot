package bot.listeners;

import com.zgamelogic.annotations.DiscordController;
import com.zgamelogic.annotations.DiscordMapping;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.utils.FileUpload;
import org.springframework.context.annotation.Bean;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.List;

@DiscordController
@Slf4j
public class QuoteBot {

    @DiscordMapping(Id = "Quote")
    private void userQuoted(MessageContextInteractionEvent event){
        event.deferReply().queue();
        User user = event.getTarget().getAuthor();
        String quote = event.getTarget().getContentDisplay().replaceAll("[^\\x00-\\x7F]", "").trim();
        if(quote.isEmpty()){
            event.getHook().sendMessage("Nothing seems to be quotable here").setEphemeral(true).queue();
            return;
        }
        try {
            BufferedImage avatar = ImageIO.read(new URL(user.getEffectiveAvatarUrl()));
            BufferedImage bufferedImage = new BufferedImage(381, 127, BufferedImage.TYPE_INT_RGB);
            Graphics2D pane = bufferedImage.createGraphics();
            pane.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            pane.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            pane.drawImage(avatar, 0, 0, null);
            pane.setColor(Color.WHITE);

            List<String> formattedQuote = new LinkedList<>();
            String font = randomFont();
            int fontSize = 30;
            int fontHeight = pane.getFontMetrics().getHeight();
            while(formattedQuote.isEmpty() || formattedQuote.size() * fontHeight > 100){
                if(fontSize < 1){
                    event.getHook().sendMessage("This is too long to quote").setEphemeral(true).queue();
                    return;
                }
                pane.setFont(loadCustomFont(font, fontSize--));
                fontHeight = pane.getFontMetrics().getHeight();
                formattedQuote = getQuoteLines(pane, quote, 240);
            }
            drawQuoteLines(
                    pane,
                    formattedQuote,
                    135,
                    pane.getFontMetrics().getHeight(),
                    user.getEffectiveName()
            );
            pane.dispose();

            // Convert BufferedImage to InputStream
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "png", os);
            InputStream is = new ByteArrayInputStream(os.toByteArray());
            event.getHook().sendFiles(FileUpload.fromData(is, "quote.png")).queue();
        } catch (IOException e) {
            log.error("Unable to make quote", e);
            event.getHook().sendMessage("Unable to make quote").setEphemeral(true).queue();
        }
    }

    private List<String> getQuoteLines(Graphics2D g, String text, int width){
        FontMetrics fm = g.getFontMetrics();

        List<String> lines = new ArrayList<>();
        String[] words = text.split("\\s+");
        StringBuilder currentLine = new StringBuilder(words[0]);

        for (int i = 1; i < words.length; i++) {
            String word = words[i];
            // Check if adding the next word exceeds the width
            if (fm.stringWidth(currentLine + " " + word) <= width) {
                currentLine.append(" ").append(word);
            } else {
                lines.add(currentLine.toString());
                currentLine = new StringBuilder(word);
            }
        }
        lines.add(currentLine.toString());
        return lines;
    }

    private void drawQuoteLines(Graphics2D g, List<String> lines, int x, int y, String username){
        FontMetrics fm = g.getFontMetrics();
        int lineHeight = fm.getHeight();

        for (String line : lines) {
            g.drawString(line, x, y);
            y += lineHeight;
        }

        g.setFont(new Font("SansSerif", Font.ITALIC, 12));
        g.setColor(Color.WHITE);
        int height = g.getFontMetrics().getHeight();
        int place = Math.min(y, 127 - height + 5);
        g.drawString("- " + username, x, place);
    }

    private Font loadCustomFont(String name, float size) {
        try {
            InputStream is = QuoteBot.class.getResourceAsStream("/assets/fonts/" + name + "-Regular.ttf");
            Font customFont = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(size);
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(customFont);
            return customFont;
        } catch (Exception e) {
            log.error("Unable to load custom font", e);
            return null;
        }
    }

    private String randomFont(){
        List<String> names = List.of(
                "Baskervville",
                "Lato",
                "Milonga",
                "Spirax",
                "Tangerine",
                "Yellowtail"
        );
        return names.get(new Random().nextInt(0, names.size()));
    }

    @Bean
    private CommandData commands(){
        return Commands.message("Quote");
    }
}
