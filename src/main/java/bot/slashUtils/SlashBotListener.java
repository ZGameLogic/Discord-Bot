package bot.slashUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class SlashBotListener extends ListenerAdapter {
	private Logger logger = LoggerFactory.getLogger(SlashBotListener.class);
	
	/**
	 * Login event
	 */
	@Override
	public void onReady(ReadyEvent event) {
		logger.info("Slash bot listener activated");
	}
	
	@Override
	public void onSlashCommand(SlashCommandEvent event) {
		switch(event.getName()) {
		case "teams":
			generateTeam(event);
			break;
		}
	}
	
	private void generateTeam(SlashCommandEvent event) {
		
	}
}
