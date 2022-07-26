package bot.steam;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import controllers.steam.SteamAPI;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class SteamListener extends ListenerAdapter {

	private Logger logger = LoggerFactory.getLogger(SteamListener.class);
	
	@Override
	public void onReady(ReadyEvent event) {
		logger.info("Steam bot listener activated");
	}

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		if(event.getMessage().getContentRaw().contains("https://store.steampowered.com/app/")) {
			for(String word : event.getMessage().getContentRaw().split(" ")) {
				if(word.contains("https://store.steampowered.com/app/")) {
					String appID = word.replace("https://store.steampowered.com/app/", "").split("/")[0];
					SteamAppData SAD = SteamAPI.appReviews(appID);
					if(event.isFromGuild()) {
						event.getChannel().asTextChannel().sendMessageEmbeds(SAD.generateEmbeds()).queue();
					} else {
						event.getChannel().asPrivateChannel().sendMessageEmbeds(SAD.generateEmbeds()).queue();
					}
				}
			}
		}
	}
}
