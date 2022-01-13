package webhook.listeners;

import data.ConfigLoader;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.TextChannel;

public class WebHookListener {
	
	private static TextChannel channel;
	
	public WebHookListener(ConfigLoader cl, JDA bot) {
		channel = bot.getGuildById(cl.getGuildID()).getTextChannelById(cl.getBitbucketID());
	}
	
	public static TextChannel getChannel() {
		return channel;
	}

}
