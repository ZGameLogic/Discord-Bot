package EventBot.listeners;

import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class EventBotListener extends ListenerAdapter {

	
	/**
	 * Login event
	 */
	@Override
	public void onReady(ReadyEvent event) {	
		System.out.println("Event Listner started...");
	}
}
