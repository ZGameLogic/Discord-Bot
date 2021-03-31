package webhook.listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import data.ConfigLoader;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.TextChannel;

public class WebHookListener {
	
	private Logger logger = LoggerFactory.getLogger(WebHookListener.class);
	
	private static TextChannel channel;
	
	public WebHookListener(ConfigLoader cl, JDA bot) {
		
		channel = bot.getGuildById(cl.getBitbucketGuildIDs().get(0)).getTextChannelById(cl.getBitbucketGuildIDs().get(1));
		
		logger.info("Webhook Listener started...");
	}
	
	public static TextChannel getChannel() {
		return channel;
	}
}
