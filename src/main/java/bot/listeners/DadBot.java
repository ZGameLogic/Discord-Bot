package bot.listeners;

import com.zgamelogic.AdvancedListenerAdapter;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class DadBot extends AdvancedListenerAdapter {
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if(!event.isFromGuild()) return;
        if(event.getAuthor().isBot()) return;
        String message = event.getMessage().getContentRaw().toLowerCase().replaceAll("'", "");
        if(message.startsWith("im ") || message.contains(" im ")){
            String[] messageArray = message.split(" ");
            String dad = "";
            for(int i = 0; i < messageArray.length; i++){
                if(messageArray[i].equals("im")){
                    if(i < messageArray.length - 1){
                        dad = messageArray[i+1];
                        break;
                    }
                }
            }
            if(!dad.equals("")) event.getMessage().reply("Hi " + dad + ", I'm dad").queue();
        }
    }
}
