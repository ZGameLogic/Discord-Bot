package com.zgamelogic.bot.listeners;

import com.zgamelogic.annotations.DiscordController;
import com.zgamelogic.annotations.DiscordMapping;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

@DiscordController
public class DadBot {
    @DiscordMapping
    public void messageReceived(MessageReceivedEvent event) {
        if(!event.isFromGuild()) return;
        if(event.getAuthor().getIdLong() == 195174230281814016L) { // Karisa's stuff
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
    }
}
