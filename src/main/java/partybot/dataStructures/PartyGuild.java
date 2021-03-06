package partybot.dataStructures;

import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Scanner;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import partybot.listeners.PartyRoom;

public class PartyGuild {

	// Guild all these girls belong too
	private Guild guild;

	// Command text channel
	private TextChannel commandChannel;

	// Party chat room category
	private Category partyChatroomCategory;

	// Channel to create a chat room
	private VoiceChannel createRoom;

	// ignored channels to not delete
	private LinkedList<VoiceChannel> ignoredChannels;

	// Links between the voice channels and their text channels
	private Map<VoiceChannel, TextChannel> channelLinks;

	public PartyGuild(Guild guild) {

		this.guild = guild;
		
		// get the guild file
		File guildProperties = new File("BotData\\GuildData\\" + guild.getId() + "\\data.txt");

		if (guildProperties.exists()) {
			// if it exists, load that data in
			try {
				Map<String, String> data = loadGuildDataFile(guildProperties);

				commandChannel = guild.getTextChannelById(data.get("Command.Channel.ID"));
				partyChatroomCategory = guild.getCategoryById(data.get("Party.Chatroom.Category.ID"));
				createRoom = guild.getVoiceChannelById(data.get("Create.Voice.Channel.ID"));

			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}

		} else {
			try {
				createGuildDataFile(guildProperties);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		if(checkPartyBotCommand()) {
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



	private Map<String, String> loadGuildDataFile(File guildData) throws FileNotFoundException {
		Map<String, String> data = new HashMap<String, String>();
		Scanner fileInput = new Scanner(guildData);
		while (fileInput.hasNextLine()) {
			String line = fileInput.nextLine();
			data.put(line.split("=")[0], line.split("=")[1]);
		}
		fileInput.close();

		return data;
	}


	private void createGuildDataFile(File guildData) throws IOException {
		guildData.getParentFile().mkdirs();
		guildData.createNewFile();
		PrintWriter output = new PrintWriter(guildData);

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
		
		output.println("Command.Channel.ID=" + commandChannel.getId());
		output.println("Party.Chatroom.Category.ID=" + partyChatroomCategory.getId());
		output.println("Create.Voice.Channel.ID=" + createRoom.getId());

		output.flush();
		output.close();
	}
	
	/**
	 * Check the command channel to see what party bot version message is inside
	 * @return true if the message is out of date, false if the message is up to date
	 */
	private boolean checkPartyBotCommand() {
		if(commandChannel.getTopic() == null || !commandChannel.getTopic().contains(PartyRoom.VERSION)) {
			return true;
		}
		
		return false;
	}
	
	private void postPartybotCommand() {
		
		File commandMessageIDFile = new File("BotData\\GuildData\\" + guild.getId() + "\\CommandMessageID.txt");
		
		// delete previous message
		if(commandMessageIDFile.exists()) {
			try {
				Scanner in = new Scanner(commandMessageIDFile);
				Long id = in.nextLong();
				in.close();
				if(commandChannel.retrieveMessageById(id) != null) {
					commandChannel.deleteMessageById(id).queue();
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}else {
			commandMessageIDFile.getParentFile().mkdirs();
			try {
				commandMessageIDFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		commandChannel.getManager().setTopic("Commands for bot version: " + PartyRoom.VERSION).queue();
		
		EmbedBuilder eb = new EmbedBuilder();

		eb.setTitle("Commands for shlongbot party system");
		eb.setColor(Color.magenta);
		eb.setDescription("These are the commands you can use to control the party system.");
		eb.addField("limit <user limit>", "Limits the number of people that can join the chat room", false);
		eb.addField("rename <room name>", "Renames the chat room and any associated text chat rooms to the new name", false);
		eb.addField("create text", "Creates a private text channel for this chat room", false);
		eb.addField("delete text", "Deletes any text channel for this chat room", false);
		
		eb.setFooter("For version: " + PartyRoom.VERSION);

		MessageEmbed embed = eb.build();

		// store new message
		Long messageID = commandChannel.sendMessage(embed).complete().getIdLong();
		
		try {
			PrintWriter output = new PrintWriter(new File("BotData\\GuildData\\" + guild.getId() + "\\CommandMessageID.txt"));
			
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
	
	

}
