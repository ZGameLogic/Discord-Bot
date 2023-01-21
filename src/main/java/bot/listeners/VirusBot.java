package bot.listeners;

import com.zgamelogic.AdvancedListenerAdapter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;

public class VirusBot extends AdvancedListenerAdapter {

    private JDA bot;

    @Override
    public void onReady(ReadyEvent event) {
        bot = event.getJDA();
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if(event.getAuthor().isBot()) return;
        if(event.getMessage().getAttachments().isEmpty()) return;
        for(Message.Attachment e: event.getMessage().getAttachments()){
            String url = e.getProxy().getUrl();
        }
    }

    private void checkUrl(String URL, String guildId, String channelId){

    }
}
