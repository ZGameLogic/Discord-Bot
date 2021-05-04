package EventBot.listeners;

import java.io.File;
import java.util.Calendar;
import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import EventBot.dataStructures.DiscordEvent;
import data.ConfigLoader;
import data.DataCacher;
import java.awt.Color;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class EventBotListener extends ListenerAdapter {

	private Logger logger = LoggerFactory.getLogger(EventBotListener.class);
	
	// event list to hold all events
	private static LinkedList<DiscordEvent> events;
	private static JDA bot;
	private static ConfigLoader cl;
	
	public EventBotListener(ConfigLoader cl) {
		events = new LinkedList<DiscordEvent>();
		
		File eventDir = new File("BotData\\Events");
		
		if(eventDir.exists()) {
			for(File x : eventDir.listFiles()) {
				events.add((DiscordEvent)DataCacher.loadSerialized(x.getPath()));
			}
		}else {
			eventDir.mkdirs();
		}
		
		new Timing().start();
		
		EventBotListener.cl = cl;
	}
	
	/**
	 * Login event
	 */
	@Override
	public void onReady(ReadyEvent event) {
		bot = event.getJDA();
		logger.info("Event Listener started...");
		for(DiscordEvent e : events) {
			bot.getGuildById(cl.getEventGuildIDs().get(0)).getTextChannelById(cl.getEventChannelIDs().get(0)).editMessageById(e.getMessageID(), e.getMessage()).queue();
		}
	}
	
	@Override
	public void onMessageReactionAdd(MessageReactionAddEvent event) {
		try {
			if (!event.getUser().isBot()) {
				DiscordEvent e = null;
				if (event.getReaction().toString().contains("U+1f514")
						&& ((e = getEventByID(event.getMessageId())) != null)) {
					e.getUserIDs().add(event.getUserId());
					DataCacher.saveSerialized(e, e.getFilePath());

					EmbedBuilder emb = new EmbedBuilder();
					emb.setTitle("You have subscribed to event: " + e.getName());
					emb.setDescription(
							"You will be messaged by me the day of the event as well as 15 minutes before the event starts. "
									+ "You will also be notified of any changes to the event time.");
					emb.setColor(Color.MAGENTA);
					event.getUser().openPrivateChannel().complete().sendMessage(emb.build()).queue();
				}
			}
		} catch (IllegalStateException e1) {

		}
	}
	
	@Override
	public void onMessageReactionRemove(MessageReactionRemoveEvent event) {
		try {
			if (!event.getUser().isBot()) {
				DiscordEvent e = null;
				if (event.getReaction().toString().contains("U+1f514")
						&& ((e = getEventByID(event.getMessageId())) != null)) {
					e.getUserIDs().remove(event.getUser().getId());
					DataCacher.saveSerialized(e, e.getFilePath());
					event.getUser().openPrivateChannel().complete().sendMessage("You have unsubscribed to the event:" + e.getName()).queue();
				}
			}
		} catch (IllegalStateException e1) {

		}
	}
	
	public DiscordEvent getEventByID(String id) {
		for(DiscordEvent e : events) {
			if(e.getMessageID() != null && e.getMessageID().equals(id)) {
				return e;
			}
		}
		return null;
	}
	
	public static void addEvent(DiscordEvent event) {
		events.add(event);
		TextChannel announ = bot.getGuildById(cl.getEventGuildIDs().get(0)).getTextChannelById(cl.getEventChannelIDs().get(0));
		
		Message message = announ.sendMessage(event.getMessage()).complete();
		
		event.setMessageID(message.getId());
		DataCacher.saveSerialized(event, event.getFilePath());
		message.addReaction("U+1F514").queue();
	}
	
	public static void editEvent() {
		
	}
	
	private class Timing extends Thread {
		public void run() {
			while(true) {
				//sleep thread for one minute
				try {
					Thread.sleep(6000);
				} catch (InterruptedException e) {
					
				}
				
				// TODO parse events and update them on discord
				Calendar currentTime = Calendar.getInstance();
				LinkedList<DiscordEvent> removeEvents = new LinkedList<DiscordEvent>();
				for(DiscordEvent e : events) {
					if(currentTime.after(e.getDate())) {
						removeEvents.add(e);
					}else {
						bot.getGuildById(cl.getEventGuildIDs().get(0)).getTextChannelById(cl.getEventChannelIDs().get(0)).editMessageById(e.getMessageID(), e.getMessage()).queue();
					}
				}
				
				// remove the events
				for(DiscordEvent e : removeEvents) {
					new File(e.getFilePath()).delete();
				}
				
				events.removeAll(removeEvents);
			}
		}
	}
}
