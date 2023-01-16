package bot.listeners;

import com.zgamelogic.AdvancedListenerAdapter;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class WordleBot extends AdvancedListenerAdapter {

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if(event.getMessage().getContentRaw().startsWith("Wordle ")){
            try {
                String line = event.getMessage().getContentRaw().replace("Wordle ", "").split("\n")[0];
                int number = Integer.parseInt(line.split(" ")[0]);
                line = line.replace(number + "", "").trim();
                int guesses = Integer.parseInt(line.charAt(0) + "");
                if(guesses <= 2){
                    event.getMessage().reply("Nice work on getting Wordle:" + number + " in "
                    + guesses + (guesses == 1 ? " guess" : " guesses")).queue();
                }
            } catch (Exception ignored){}
        }
    }
}
