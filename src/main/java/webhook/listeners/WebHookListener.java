package webhook.listeners;

import data.ConfigLoader;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.TextChannel;

public class WebHookListener {
	
	private static TextChannel channel;
	
	public WebHookListener(ConfigLoader cl, JDA bot) {
		
		channel = bot.getGuildById(cl.getBitbucketGuildIDs().get(0)).getTextChannelById(cl.getBitbucketGuildIDs().get(1));
	}
	
	public static TextChannel getChannel() {
		return channel;
	}
}
