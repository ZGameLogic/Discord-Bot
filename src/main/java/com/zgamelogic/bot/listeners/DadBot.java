package com.zgamelogic.bot.listeners;

import com.zgamelogic.annotations.DiscordController;
import com.zgamelogic.annotations.DiscordMapping;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.utils.FileUpload;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.Random;

@DiscordController
public class DadBot {
    @Value("${amrit.enabled}")
    private boolean enabled;

    @DiscordMapping
    public void messageReceived(MessageReceivedEvent event) {
        if(!event.isFromGuild()) return;
        if(event.getAuthor().getIdLong() == 102923614344482816L) { // Karisa's stuff
            String message = event.getMessage().getContentRaw().toLowerCase().replaceAll("'", "").replaceAll("’", "");
            if(message.startsWith("im ") || message.contains(" im ")){
                String[] messageArray = message.split(" ");
                StringBuilder dad = new StringBuilder();
                boolean adding = false;
                for (String s : messageArray) {
                    if (adding) {
                        dad.append(s.replace(".", "")).append(" ");
                        if (s.contains(".")) {
                            break;
                        }
                    }
                    if (s.equals("im")) {
                        adding = true;
                    }
                }
                dad = new StringBuilder(dad.toString().trim());
                if(!dad.toString().isEmpty()) event.getMessage().reply("Hi " + dad + ", I'm Dad").queue();
            }
        }
        if(enabled && event.getAuthor().getIdLong() == 195174230281814016L){ // Amrit's stuff
            if(event.getMessage().getContentRaw().contains("...")){
                try {
                    int meme = new Random().nextInt(1, 16);
                    event.getChannel().sendFiles(FileUpload.fromData(new ClassPathResource("assets/Amrit/amritmeme0" + meme + ".jpg").getFile())).queue();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}