package webhook.listeners;

import java.awt.Color;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import data.ConfigLoader;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class WebHookReactionListener extends ListenerAdapter {
	
	private Logger logger = LoggerFactory.getLogger(WebHookReactionListener.class);
	
	private TextChannel channel;
	private ConfigLoader cl;
	
	private static Message currentMessage;
	
	public WebHookReactionListener(ConfigLoader cl) {
		this.cl = cl;
	}
	
	/**
	 * Login event
	 */
	@Override
	public void onReady(ReadyEvent event) {
		logger.info("Webhook Reaction Listener started...");
		channel = event.getJDA().getGuildById(cl.getBitbucketGuildIDs().get(0)).getTextChannelById(cl.getBitbucketGuildIDs().get(1));
	}
	
	
	@Override
	public void onMessageReactionAdd(MessageReactionAddEvent event) {
		if(!event.getUser().isBot() && event.getChannel().equals(channel)) {
			if(event.getReaction().toString().contains("RE:U+1f3d7")){
				currentMessage = event.retrieveMessage().complete();
				
				EmbedBuilder eb = new EmbedBuilder();
				MessageEmbed old = event.retrieveMessage().complete().getEmbeds().get(0);
				
				eb.setTitle(old.getTitle(),"https://zgamelogic.com:7990/projects/BSPR/repos/discord-bot/browse");
				eb.setAuthor(old.getAuthor().getName(), old.getAuthor().getUrl());
				
				for(Field x : old.getFields()) {
					eb.addField(x);
				}
				
				eb.setColor(Color.BLUE);
				
				event.retrieveMessage().complete().editMessage(eb.build()).complete();
			}
		}
	}
	
	public static void changeStatus(Color color) {
		
		EmbedBuilder eb = new EmbedBuilder();
		MessageEmbed old = currentMessage.getEmbeds().get(0);
		
		eb.setTitle(old.getTitle(),"https://zgamelogic.com:7990/projects/BSPR/repos/discord-bot/browse");
		eb.setAuthor(old.getAuthor().getName(), old.getAuthor().getUrl());
		
		for(Field x : old.getFields()) {
			eb.addField(x);
		}
		
		eb.setColor(color);
		
		currentMessage.editMessage(eb.build()).complete();
	}
}
