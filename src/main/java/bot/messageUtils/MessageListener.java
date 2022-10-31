package bot.messageUtils;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class MessageListener extends ListenerAdapter {

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
       if(event.getAuthor().getIdLong() == 262458179563159563l){ // check if its rob
           String message = event.getMessage().getContentRaw();
           if(message.contains("work") && message.contains("go") && message.contains("to") && message.contains("should")){
               event.getChannel().sendMessage("Yes, you should go to work.").queue();
           }
       }
    }
}
