package EventBot.listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class EventBotListener extends ListenerAdapter {

	private Logger logger = LoggerFactory.getLogger(EventBotListener.class);
	
	/**
	 * Login event
	 */
	@Override
	public void onReady(ReadyEvent event) {	
		logger.info("Event Listener started...");
	}
}
