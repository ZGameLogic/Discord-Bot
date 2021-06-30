package musicBot.listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import data.ConfigLoader;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class MusicBotListener extends ListenerAdapter {
	
	private Logger logger = LoggerFactory.getLogger(MusicBotListener.class);
	
	public MusicBotListener(ConfigLoader cl) {
		
	}
	
	/**
	 * Login event
	 */
	@Override
	public void onReady(ReadyEvent event) {
		logger.info("Music bot Listener started...");
		//event.getJDA().getGuildById(738850921706029168l).getAudioManager().openAudioConnection(event.getJDA().getGuildById(738850921706029168l).getVoiceChannelById(738850921706029172l));
	}

}
