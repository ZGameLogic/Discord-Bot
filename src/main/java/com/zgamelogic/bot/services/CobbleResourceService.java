package com.zgamelogic.bot.services;

import com.zgamelogic.annotations.DiscordController;
import com.zgamelogic.annotations.DiscordMapping;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Icon;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.List;

@Service
@DiscordController
public class CobbleResourceService {
    private final ResourcePatternResolver resourcePatternResolver;
    private final ResourceLoader resourceLoader;
    private final long discordGuildId;
    private final List<String> maleFirstNames;
    private final List<String> femaleFirstNames;
    private final List<String> lastNames;
    @Getter
    private final Resource cobbleLogo;

    private HashMap<String, RichCustomEmoji> emojis;
    private HashMap<String, Command.Subcommand> commands;
    private Guild discordGuild;

    public CobbleResourceService(@Value("${discord.guild}") long discordGuildId, ResourcePatternResolver resourcePatternResolver, ResourceLoader resourceLoader) throws IOException {
        this.discordGuildId = discordGuildId;
        this.resourcePatternResolver = resourcePatternResolver;
        this.resourceLoader = resourceLoader;
        this.maleFirstNames = new LinkedList<>();
        this.femaleFirstNames = new LinkedList<>();
        this.lastNames = new LinkedList<>();
        cobbleLogo = resourceLoader.getResource("classpath:assets/Cobble/cobble-logo.png");
        loadNames();
    }

    /**
     * Get a mentionable string of an emoji
     * @param key key of the mapping
     * @return String mentionable
     */
    public String em(String key){
        try {
            return emojis.get(key).getAsMention();
        } catch(Exception e){
            return "`Emoji not found`";
        }
    }

    /**
     * Get a mentionable string of a slash command
     * @param key key of the mapping
     * @return String mentionable
     */
    public String cm(String key){
        try {
            return commands.get(key).getAsMention();
        } catch(Exception e){
            return "`Slash command not found`";
        }
    }

    public String randomName(boolean male) {
        Random random = new Random();
        String firstName = male ?
            maleFirstNames.get(random.nextInt(maleFirstNames.size())) :
            femaleFirstNames.get(random.nextInt(femaleFirstNames.size()));
        String lastName = lastNames.get(random.nextInt(lastNames.size()));
        return firstName + " " + lastName;
    }

    public BufferedImage mapAppearance(String appearance) throws IOException {
        int index = 1;
        int skinColorOffset = Integer.parseInt(appearance.substring(index, (index++) + 1));
        int hairColorOffset = Integer.parseInt(appearance.substring(index, (index++) + 1));
        int hairStyleOffset = Integer.parseInt(appearance.substring(index, (index++) + 1));
        int eyeColorOffset = Integer.parseInt(appearance.substring(index, (index++) + 1));
        int facialHairOffset = Integer.parseInt(appearance.substring(index, (index++) + 1));
        int shirtColorOffset = Integer.parseInt(appearance.substring(index, (index++) + 1));
        int pantColorOffset = Integer.parseInt(appearance.substring(index, index + 1));
        List<Color> colors = List.of(
            new Color(0, 0, 0),
            new Color(189, 189, 189),
            new Color(138, 79, 15),
            new Color(246, 189, 113),
            new Color(250, 137, 43)
        );

        BufferedImage npc = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        Graphics2D pane = npc.createGraphics();
        // skin
        BufferedImage skins = ImageIO.read(resourceLoader.getResource("classpath:/assets/Cobble/Npc Assets/skin.png").getInputStream());
        pane.drawImage(skins.getSubimage(skinColorOffset * 32, 0, 32, 32), 0, 0, null);
        // shirt
        BufferedImage shirts = ImageIO.read(resourceLoader.getResource("classpath:/assets/Cobble/Npc Assets/shirt.png").getInputStream());
        pane.drawImage(shirts.getSubimage(shirtColorOffset * 32, 0, 32, 32), 0, 0, null);
        // pants
        BufferedImage pants = ImageIO.read(resourceLoader.getResource("classpath:/assets/Cobble/Npc Assets/pants.png").getInputStream());
        pane.drawImage(pants.getSubimage(pantColorOffset * 32, 0, 32, 32), 0, 0, null);
        // eyes
        BufferedImage eyes = ImageIO.read(resourceLoader.getResource("classpath:/assets/Cobble/Npc Assets/eyes.png").getInputStream());
        pane.drawImage(eyes.getSubimage(eyeColorOffset * 32, 0, 32, 32), 0, 0, null);
        // hair
        BufferedImage hair = ImageIO.read(resourceLoader.getResource("classpath:/assets/Cobble/Npc Assets/hair.png").getInputStream());
        hair = applyColorToWhite(hair.getSubimage(hairStyleOffset * 32, 0, 32, 32), colors.get(hairColorOffset));
        pane.drawImage(hair, 0, 0, null);
        // facial hair
        BufferedImage fhair = ImageIO.read(resourceLoader.getResource("classpath:/assets/Cobble/Npc Assets/facial-hair.png").getInputStream());
        fhair = applyColorToWhite(fhair.getSubimage(facialHairOffset * 32, 0, 32, 32), colors.get(hairColorOffset));
        pane.drawImage(fhair, 0, 0, null);

        pane.dispose();
        int newWidth = npc.getWidth() * 3;
        int newHeight = npc.getHeight() * 3;
        BufferedImage scaledNpc = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = scaledNpc.createGraphics();
        g2d.drawImage(npc, 0, 0, newWidth, newHeight, null);
        g2d.dispose();
        return scaledNpc;
    }

    private BufferedImage applyColorToWhite(BufferedImage image, Color color) {
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                int pixel = image.getRGB(x, y);
                Color pixelColor = new Color(pixel, true);
                if (pixelColor.getRed() == 255 && pixelColor.getGreen() == 255 && pixelColor.getBlue() == 255 && pixelColor.getAlpha() > 0) {
                    image.setRGB(x, y, color.getRGB());
                }
            }
        }
        return image;
    }

    public InputStream mapAppearanceAsStream(String appearance) throws IOException {
        BufferedImage image = mapAppearance(appearance);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        return new ByteArrayInputStream(baos.toByteArray());
    }

    @DiscordMapping
    private void onReady(ReadyEvent event) throws IOException {
        discordGuild = event.getJDA().getGuildById(discordGuildId);
        mapEmojis();
        mapCommands();
    }

    private void loadNames() throws IOException {
        Scanner in = new Scanner(resourceLoader.getResource("classpath:assets/Cobble/female_first_names.txt").getInputStream());
        while (in.hasNextLine()) femaleFirstNames.add(in.nextLine());
        in.close();
        in = new Scanner(resourceLoader.getResource("classpath:assets/Cobble/male_first_names.txt").getInputStream());
        while (in.hasNextLine()) maleFirstNames.add(in.nextLine());
        in.close();
        in = new Scanner(resourceLoader.getResource("classpath:assets/Cobble/last_names.txt").getInputStream());
        while (in.hasNextLine()) lastNames.add(in.nextLine());
        in.close();
    }

    private void mapCommands(){
        commands = new HashMap<>();
        discordGuild.getJDA().retrieveCommands().complete().stream().filter(command -> command.getName().equals("cobble"))
            .forEach(command -> {
                command.getSubcommandGroups().forEach(subcommandGroup -> {
                    subcommandGroup.getSubcommands().forEach(subcommand ->
                        commands.put(command.getName() + " " + subcommandGroup.getName() + " " + subcommand.getName(), subcommand)
                    );
                });
                command.getSubcommands().forEach(subcommand ->
                    commands.put(command.getName() + " " + subcommand.getName(), subcommand)
                );
            });
    }

    private void mapEmojis() throws IOException {
        emojis = new HashMap<>();
        Arrays.stream(resourcePatternResolver.getResources("classpath:assets/Cobble/Emojis/*")).forEach(resource -> {
            try {
                String filename = resource.getFilename();
                String iconName = filename.replace(".png", "");
                List<RichCustomEmoji> emojiList = discordGuild.getEmojisByName(iconName, true);
                RichCustomEmoji emoji;
                if(emojiList.isEmpty()) {
                    Icon icon = Icon.from(resource.getInputStream());
                    emoji = discordGuild.createEmoji(iconName, icon).complete();
                } else {
                    emoji = emojiList.get(0);
                }
                emojis.put(emoji.getName(), emoji);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
