package setup.listeners;

import java.awt.Color;
import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.react.PrivateMessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.priv.react.PrivateMessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import setup.data.Guild;
import setup.data.Guild.GuildType;

public class SetupListener extends ListenerAdapter {

	private Logger logger = LoggerFactory.getLogger(SetupListener.class);
	
	public final static File GUILD_DIR = new File("BotData\\GuildData");
	private HashMap<String, Guild> guilds;
	
	public SetupListener() {
		
		guilds = new HashMap<String, Guild>();
		
		if(GUILD_DIR.exists()) {
			for(File x : GUILD_DIR.listFiles()) {
				Guild current = new Guild(x);
				guilds.put(current.getGuildID(), current);
			}
		} else {
			GUILD_DIR.mkdirs();
		}
	}
	
	/**
	 * Login event
	 */
	@Override
	public void onReady(ReadyEvent event) {
		logger.info("Setup bot Listener started...");
		for(net.dv8tion.jda.api.entities.Guild x : event.getJDA().getGuilds()) {
			// We need to open the private channels back up so we can see message reactions
			event.getJDA().openPrivateChannelById(x.getOwnerId()).queue();
			if(getGuildById(x.getId()) == null) {
				newGuild(x);
			}
		}
	}
	
	@Override
	public void onGuildJoin(GuildJoinEvent event) {
		newGuild(event.getGuild());
	}
	
	@Override
	public void onGuildLeave(GuildLeaveEvent event) {
		File oldGuildFile = new File(GUILD_DIR + "\\" + event.getGuild().getId() + ".dat");
		oldGuildFile.delete();
		guilds.remove(event.getGuild().getId());
	}
	
	@Override
	public void onPrivateMessageReactionAdd(PrivateMessageReactionAddEvent event) {
		if(!event.getUser().isBot() && isOwner(event.getUserId()) && isSetupMessage(event.getMessageId())) {
			String emote = event.getReaction().toString();
			if(emote.contains("U+34U+fe0fU+20e3")) {
				getGuildByMessageID(event.getMessageId()).addType(GuildType.Party);
				getGuildByMessageID(event.getMessageId()).setupParty(event.getJDA().getGuildById(getGuildByMessageID(event.getMessageId()).getGuildID()));
			}else if(emote.contains("U+31U+fe0fU+20e3")) {
				getGuildByMessageID(event.getMessageId()).addType(GuildType.Code);
			}else if(emote.contains("U+32U+fe0fU+20e3")) {
				getGuildByMessageID(event.getMessageId()).addType(GuildType.Event);
			}else if(emote.contains("U+33U+fe0fU+20e3")) {
				getGuildByMessageID(event.getMessageId()).addType(GuildType.Meme);
			}else if(emote.contains("U+35U+fe0fU+20e3")) {
				getGuildByMessageID(event.getMessageId()).addType(GuildType.API);
			}
		}
	}
	
	@Override
	public void onPrivateMessageReactionRemove(PrivateMessageReactionRemoveEvent event) {
		if(!event.getUser().isBot() && isOwner(event.getUserId()) && isSetupMessage(event.getMessageId())) {
			String emote = event.getReaction().toString();
			if(emote.contains("U+34U+fe0fU+20e3")) {
				getGuildByMessageID(event.getMessageId()).removeType(GuildType.Party);
			}else if(emote.contains("U+31U+fe0fU+20e3")) {
				getGuildByMessageID(event.getMessageId()).removeType(GuildType.Code);
			}else if(emote.contains("U+32U+fe0fU+20e3")) {
				getGuildByMessageID(event.getMessageId()).removeType(GuildType.Event);
			}else if(emote.contains("U+33U+fe0fU+20e3")) {
				getGuildByMessageID(event.getMessageId()).removeType(GuildType.Meme);
			}else if(emote.contains("U+35U+fe0fU+20e3")) {
				getGuildByMessageID(event.getMessageId()).removeType(GuildType.API);
			}
		}
	}
	
	@Override
	public void onPrivateMessageReceived(PrivateMessageReceivedEvent event) {
		if(!event.getAuthor().isBot()) {
			
		}
	}
	
	/**
	 * Parses through all guilds to see if this is a valid API Token for the guild
	 */
	public boolean isValidAPIToken(String token) {
		for(Guild x : guilds.values()) {
			if(x.getAPIToken().equals(token)) {
				return true;
			}
		}
		return false;
	}
	
	public Guild getGuildById(String id) {
		for(Guild x : guilds.values()) {
			if(x.getGuildID().equals(id)) {
				return x;
			}
		}
		return null;
	}
	
	public LinkedList<String> getGuildIDs(GuildType guildType){
		LinkedList<String> guildIDS = new LinkedList<String>();
		for(Guild x : guilds.values()) {
			if(x.isType(guildType)) {
				guildIDS.add(x.getGuildID());
			}
		}
		return guildIDS;
	}
	
	private void newGuild(net.dv8tion.jda.api.entities.Guild guild) {
		Message message = guild.getOwner().getUser().openPrivateChannel().complete().sendMessage(createGuildWelcome(guild.getName()).build()).complete();
		message.addReaction("U+0031 U+FE0F U+20E3").queue();
		message.addReaction("U+0032 U+FE0F U+20E3").queue();
		message.addReaction("U+0033 U+FE0F U+20E3").queue();
		message.addReaction("U+0034 U+FE0F U+20E3").queue();
		message.addReaction("U+0035 U+FE0F U+20E3").queue();
		Guild newGuild = new Guild(guild.getId(), message.getId(), guild.getOwnerId());
		guilds.put(newGuild.getGuildID(), newGuild);
	}
	
	private Guild getGuildByMessageID(String id) {
		for(Guild x : guilds.values()) {
			if(x.getCreateGuildMessageID().equals(id)) {
				return x;
			}
		}
		return null;
	}
	
	/**
	 * Parses through all the guilds to make sure that the user sending in these requests are an owner
	 * @param id ID of the user firing the event
	 * @return true if the user is a guild owner, false if not
	 */
	private boolean isOwner(String id) {
		for(Guild x : guilds.values()) {
			if(x.getGuildOwnerID().equals(id)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Parses through all the guilds to make sure that the message is a valid guild setup message
	 * @param id ID of the message
	 * @return true if the message is a guild message, false if not
	 */
	private boolean isSetupMessage(String id) {
		for(Guild x : guilds.values()) {
			if(x.getCreateGuildMessageID().equals(id)) {
				return true;
			}
		}
		return false;
	}
	
	private EmbedBuilder createGuildWelcome(String guildName) {
		EmbedBuilder eb = new EmbedBuilder();
		eb.setColor(Color.WHITE);
		eb.setTitle("Thank you for inviting Shlongbot to " + guildName + "!");
		
		eb.setDescription("As the server owner of " + guildName + ", you have all the rights to configure Shlongbot. "
				+ "Listed bellow is everything you need to know about what each part of the bot does. "
				+ "You can opt your server in and out of these sections by reacting or removing the reaction at any time. "
				+ "As such, DO NOT delete this message, as you only get one.");
		
		eb.addField("1. Code bot", "This part of the bot allows you to run code in discord using the markdown feature in discord.", false);
		
		eb.addField("2. Event bot", "This part of the bot allows you to host events in your server. "
				+ "It allows your users to opt in and out of events, and recieve PMs from me to remind them about the event. "
				+ "They will also recieve notifications if the event times change", false);
		
		eb.addField("3. Meme bot", "This part of the bot allows you to request a random meme by giving me a tag the meme falls under. "
				+ "Users can submit memes that get curated by admins over in the offical shlongshot server. "
				+ "Every server the memebot is in shares the same selection of memes.", false);
		
		eb.addField("4. Party bot", "This part of the bot allows you to have a chatroom where users can join and then a new chat channel gets"
				+ " made and the user moved into. Once all users leave the chatroom, it gets deleted from the server. "
				+ "This keeps the server looking nice and clean. Text channels can also be made for these created rooms for invite codes, "
				+ "conversation specific content and links, as well as anything else that only the users in the chatroom can see.", false);
		
		eb.addField("5. API Functionality", "This opts you into API functionality. If you wanted to make an event, curate memes, rename chatrooms, "
				+ "etc. using REST calls instead of discord, then you can opt into that here.", false);
		
		return eb;
	}
	
}
