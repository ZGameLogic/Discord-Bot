package bot.listeners;

import com.zgamelogic.AdvancedListenerAdapter;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.message.GenericMessageEvent;
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent;
import org.jetbrains.annotations.NotNull;

public class CardBot extends AdvancedListenerAdapter {

    @Override
    public void onGuildVoiceUpdate(GuildVoiceUpdateEvent event) {
        if(event.getChannelLeft() != null){
            // TODO update points (if the join time isnt null)
        }
        if(event.getChannelJoined() != null){
            // TODO update player joined timestamp (or keep null if in afk)
        }
    }

    @Override
    public void onGenericMessageReaction(@NotNull GenericMessageReactionEvent event) {
        super.onGenericMessageReaction(event);
        // TODO update points
    }

    @Override
    public void onGenericMessage(GenericMessageEvent event) {
        if(event.isFromGuild()){
            // TODO update points
        }
    }
}
