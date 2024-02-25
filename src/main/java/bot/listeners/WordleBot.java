package bot.listeners;

import com.zgamelogic.annotations.DiscordController;
import com.zgamelogic.annotations.DiscordMapping;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

@DiscordController
public class WordleBot {

    @DiscordMapping
    public void messageReceived(MessageReceivedEvent event) {
        if(event.getMessage().getContentRaw().startsWith("Wordle ")){
            try {
                String line = event.getMessage().getContentRaw().replace("Wordle ", "").split("\n")[0];
                int number = Integer.parseInt(line.split(" ")[0]);
                line = line.replace(number + "", "").trim();
                int guesses = Integer.parseInt(line.charAt(0) + "");
                if(guesses <= 3){
                    event.getMessage().reply("Nice work on getting Wordle:" + number + " in "
                    + guesses + (guesses == 1 ? " guess" : " guesses")).queue();
                }
            } catch (Exception ignored){}
        }
    }
}
