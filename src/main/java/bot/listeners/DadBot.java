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
            int start = message.indexOf("im") + 3;
            System.out.println(message.substring(start));
            String dad = message.split("im")[1].split(" ")[0];
            event.getMessage().reply("Hi " + dad + ", I'm dad").queue();
        }
    }
}
