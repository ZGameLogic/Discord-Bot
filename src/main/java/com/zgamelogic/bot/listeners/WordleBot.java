package com.zgamelogic.bot.listeners;

import com.zgamelogic.discord.annotations.DiscordController;
import com.zgamelogic.discord.annotations.DiscordMapping;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

@DiscordController
@Slf4j
public class WordleBot {

    @DiscordMapping
    public void messageReceived(MessageReceivedEvent event) {
        if(event.getMessage().getContentRaw().startsWith("Wordle ")){
            try {
                String line = event.getMessage().getContentRaw().replace("Wordle ", "").split("\n")[0];
                int number = Integer.parseInt(line.split(" ")[0].replace(",", ""));
                int guesses = Integer.parseInt(line.split(" ")[1].charAt(0) + "");
                if(guesses <= 3){
                    event.getMessage().reply("Nice work on getting Wordle:" + number + " in "
                    + guesses + (guesses == 1 ? " guess" : " guesses")).queue();
                }
            } catch (Exception e){
                log.error("Error responding to wordle message", e);
            }
        }
    }
}
