package partybot.dataStructures;

import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;

import data.DataCacher;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import partybot.listeners.PartyRoomListener;

public class PartyGuild implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private long commandChannelID;
	private long partyChatroomCategoryID;
	private long createVoiceChannelID;

	// Guild all these girls belong too
	transient private Guild guild;

	// Command text channel
	transient private TextChannel commandChannel;

	// Party chat room category
	transient private Category partyChatroomCategory;

	// Channel to create a chat room
	transient private VoiceChannel createRoom;

	// Ignored channels to not delete
	transient private LinkedList<VoiceChannel> ignoredChannels;

	// Links between the voice channels and their text channels
	transient private Map<VoiceChannel, TextChannel> channelLinks;

	public PartyGuild(Guild guild) {

		this.guild = guild;

		// get the guild file
		File guildProperties = new File("BotData\\GuildData\\" + guild.getId() + "\\PartyBotData");

		if (guildProperties.exists()) {
			// if it exists, load that data in
			PartyGuild SPG = (PartyGuild) DataCacher.loadSerialized(guildProperties.getPath());
			
			commandChannelID =  SPG.getCommandChannelID();
			partyChatroomCategoryID = SPG.getPartyChatroomCategoryID();
			createVoiceChannelID = SPG.getCreateVoiceChannelID();
			
			commandChannel = guild.getTextChannelById(SPG.getCommandChannelID());
			partyChatroomCategory = guild.getCategoryById(SPG.getPartyChatroomCategoryID());
			createRoom = guild.getVoiceChannelById(SPG.getCreateVoiceChannelID());

		} else {
			createGuildDataFile(guildProperties);
		}

		if (checkPartyBotCommand()) {
			postPartybotCommand();
		}

		ignoredChannels = new LinkedList<VoiceChannel>();

		ignoredChannels.add(createRoom);

		if (guild.getAfkChannel() != null) {
			ignoredChannels.add(guild.getAfkChannel());
		}

		channelLinks = new HashMap<VoiceChannel, TextChannel>();
	}

	public Category getPartyChatroomCategory() {
		return partyChatroomCategory;
	}

	public VoiceChannel getCreateRoom() {
		return createRoom;
	}

	public LinkedList<VoiceChannel> getIgnoredChannels() {
		return ignoredChannels;
	}

	public Map<VoiceChannel, TextChannel> getChannelLinks() {
		return channelLinks;
	}

	private void createGuildDataFile(File guildData) {

		// Test to see if the guild has a category yet
		if (guild.getCategoriesByName("chat rooms", true).size() > 0) {
			// it already has a chatroom category
			partyChatroomCategory = guild.getCategoriesByName("chat rooms", true).get(0);
		} else {
			// if the server doesnt have a category, make one
			partyChatroomCategory = guild.createCategory("Chat rooms").complete();
		}

		// Test to see if the guild has a category yet
		if (guild.getTextChannelsByName("shlongbot", true).size() > 0) {
			// it already has a command text chat
			commandChannel = guild.getTextChannelsByName("shlongbot", true).get(0);
		} else {
			// if the server doesnt have a command channel, make one
			commandChannel = guild.createTextChannel("shlongbot").complete();
		}

		// Test to see if the guild has a create chat
		if (guild.getVoiceChannelsByName("creat chatroom", true).size() > 0) {
			// it already has a create chat
			createRoom = guild.getVoiceChannelsByName("shlongbot", true).get(0);
		} else {
			// if the server doesnt have a create chatroom, make one
			createRoom = guild.createVoiceChannel("Create chatroom", partyChatroomCategory).complete();
		}
		
		commandChannelID =  commandChannel.getIdLong();
		partyChatroomCategoryID = partyChatroomCategory.getIdLong();
		createVoiceChannelID = createRoom.getIdLong();

		DataCacher.saveSerialized(this, guildData.getPath());
	}

	private Long getPartybotMessageCommandID() {
		File commandMessageIDFile = new File("BotData\\GuildData\\" + guild.getId() + "\\PartyCommandMessageID.txt");

		// delete previous message
		if (commandMessageIDFile.exists()) {
			try {
				Scanner in = new Scanner(commandMessageIDFile);
				Long id = in.nextLong();
				in.close();
				return id;
			} catch (FileNotFoundException e) {
			} catch (NoSuchElementException e) {
				
			}
		} else {
			commandMessageIDFile.getParentFile().mkdirs();
			try {
				commandMessageIDFile.createNewFile();
			} catch (IOException e) {
			}
		}

		return null;
	}

	/**
	 * Check the command channel to see what party bot version message is inside
	 * 
	 * @return true if the message is out of date, false if the message is up to
	 *         date
	 */
	private boolean checkPartyBotCommand() {

		Long id = getPartybotMessageCommandID();
		try {
			if (id != null && commandChannel.retrieveMessageById(id).complete() != null
					&& !commandChannel.retrieveMessageById(id).complete().getEmbeds().get(0).getFooter().getText()
							.contains(PartyRoomListener.VERSION)) {
				return true;
			}
		} catch (net.dv8tion.jda.api.exceptions.ErrorResponseException e) {
			return true;
		}

		if (id == null) {
			return true;
		}

		return false;
	}

	private void postPartybotCommand() {

		Long id = getPartybotMessageCommandID();

		// delete previous message
		if (id != null) {
			try {
				if (commandChannel.retrieveMessageById(id) != null) {
					commandChannel.deleteMessageById(id).queue();
				}
			} catch(Exception e1) {
				
			}
		}

		EmbedBuilder eb = new EmbedBuilder();

		eb.setTitle("Commands for shlongbot party system");
		eb.setColor(Color.magenta);
		eb.setDescription("These are the commands you can use to control the party system. To use these commands simply start a message anywhere with a / as this is using discords new slash commands feature");
		eb.addField("limit <user limit>", "Limits the number of people that can join the chat room", false);
		eb.addField("rename <room name>", "Renames the chat room and any associated text chat rooms to the new name",
				false);
		eb.addField("create-text", "Creates a private text channel for this chat room", false);
		eb.addField("delete-text", "Deletes any text channel for this chat room", false);

		eb.setFooter("For version: " + PartyRoomListener.VERSION);
		

		MessageEmbed embed = eb.build();

		
		// store new message
		Long messageID = commandChannel.sendMessage(embed).complete().getIdLong();

		try {
			PrintWriter output = new PrintWriter(new File("BotData\\GuildData\\" + guild.getId() + "\\PartyCommandMessageID.txt"));

			output.println(messageID);

			output.flush();
			output.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public TextChannel getCommandChannel() {
		return commandChannel;
	}

	public long getCommandChannelID() {
		return commandChannelID;
	}

	public long getPartyChatroomCategoryID() {
		return partyChatroomCategoryID;
	}

	public long getCreateVoiceChannelID() {
		return createVoiceChannelID;
	}
	
	

}
