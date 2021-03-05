package partybot.listeners;

import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Scanner;

import data.ConfigLoader;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class PartyRoom extends ListenerAdapter {
	
	//PartyBot version
	private static final String VERSION = "1.0.1";
	
	// main guild
	private Guild shlongshot;

	// voice channel category
	private Category chatRoomsCat;

	//  IDs for voice channels that create chat rooms
	private LinkedList<Long> createChatIDs;
	
	// IDs for voice channels that get ignored for the deletion process
	private LinkedList<Long> ignoredChannelIDs;
	
	// IDs for text channels that can be sent commands too
	private LinkedList<Long> textChannelIDS;
	
	// Store connections from voice channels and text channels
	private Map<VoiceChannel, TextChannel> chatroomToTextroom;
	
	public PartyRoom(ConfigLoader cl) {
		
		createChatIDs = cl.getCreateChatIDs();

		ignoredChannelIDs = cl.getIgnoredChannelIDs();

		textChannelIDS = cl.getTextChannelIDs();
		
		chatroomToTextroom = new HashMap<VoiceChannel, TextChannel>(); 
	}

	/**
	 * Login event
	 */
	@Override
	public void onReady(ReadyEvent event) {
		for (Guild x : event.getJDA().getGuilds()) {
			if (x.getName().contains("MemeBot test server") || x.getName().contains("Shlongshot")) {
				shlongshot = x;
				break;
			}
		}

		// Get the category (This is for determining if the chat room should be deleted when done)
		for (Category x : event.getJDA().getCategories()) {
			if (x.getName().contains("Chat Rooms")) {
				chatRoomsCat = x;
				break;
			}
		}
		
		// Send command message
		
		TextChannel commandChannel = null;
		
		for(int i = 0; i < textChannelIDS.size() && commandChannel == null; i++) {
			commandChannel = shlongshot.getTextChannelById(textChannelIDS.get(i));
		}
		
		if(!commandChannel.getTopic().contains(VERSION)) {
			
			File messageIDFile = new File("MessageIDs\\PartyBotCommandID.txt");
			
			// delete old message
			Long oldMessageID = -1l;
			if(messageIDFile.exists()) {
				try {
					Scanner fileInput = new Scanner(messageIDFile);
					oldMessageID = Long.parseLong(fileInput.nextLine());
					fileInput.close();
					commandChannel.deleteMessageById(oldMessageID).queue();
					
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
			}else {
				try {
					messageIDFile.getParentFile().mkdirs();
					messageIDFile.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			
			commandChannel.getManager().setTopic("Commands for bot version: " + VERSION).queue();
			
			EmbedBuilder eb = new EmbedBuilder();

			eb.setTitle("Commands for shlongbot party system");
			eb.setColor(Color.magenta);
			eb.setDescription("These are the commands you can use to control the party system.");
			eb.addField("limit <user limit>", "Limits the number of people that can join the chat room", false);
			eb.addField("rename <room name>", "Renames the chat room and any associated text chat rooms to the new name", false);
			eb.addField("create text", "Creates a private text channel for this chat room", false);
			eb.addField("delete text", "Deletes any text channel for this chat room", false);
			
			eb.setFooter("For version: " + VERSION);

			MessageEmbed embed = eb.build();

			// store new message
			Long messageID = commandChannel.sendMessage(embed).complete().getIdLong();
			try {
				PrintWriter fileout = new PrintWriter(messageIDFile);
				
				fileout.println(messageID);
				
				fileout.flush();
				fileout.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * When we get a message
	 */
	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		onMessageRecieved(event);
	}

	@Override
	public void onMessageReactionAdd(MessageReactionAddEvent event) {

	}

	@Override
	public void onMessageReactionRemove(MessageReactionRemoveEvent event) {

	}

	/**
	 * On voice chat join
	 */
	@Override
	public void onGuildVoiceJoin(GuildVoiceJoinEvent event) {
		playerJoined(event.getChannelJoined(), event.getMember());
	}

	/**
	 * On voice chat move
	 */
	@Override
	public void onGuildVoiceMove(GuildVoiceMoveEvent event) {
		playerLeft(event.getChannelLeft(), event.getMember());
		playerJoined(event.getChannelJoined(), event.getMember());
	}

	/**
	 * On voice chat leave
	 */
	@Override
	public void onGuildVoiceLeave(GuildVoiceLeaveEvent event) {
		playerLeft(event.getChannelLeft(), event.getMember());
	}

	/**
	 * Takes an incoming message event and handles it
	 * 
	 * @param event
	 */
	private void onMessageRecieved(MessageReceivedEvent event) {
		if (!event.getAuthor().isBot() && event.isFromGuild()) {
			if (textChannelIDS.contains(event.getTextChannel().getIdLong())) {

				// boolean for valid command
				boolean valid = false;
				String command = event.getMessage().getContentDisplay();

				if (command.startsWith("rename ") && command.split(" ").length > 1) {
					// Command: rename <name>
					// Renames channel to the name the user specifies

					// get the voice channel
					VoiceChannel channel = event.getMember().getVoiceState().getChannel();
					if (channel != null) {
						try {
							// rename the channel and set valid command to true
							channel.getManager().setName(command.replace("rename ", "")).queue();
							if(chatroomToTextroom.containsKey(channel)) {
								chatroomToTextroom.get(channel).getManager().setName(command.replace("rename ", "")).queue();
							}
							valid = true;
						} catch (IllegalArgumentException e1) {

						}
					}

				} else if (command.startsWith("limit ") && command.split(" ").length > 1) {
					// Command: limit <user limit number>
					// puts a limit to the max amount of users that can join the channel

					VoiceChannel channel = event.getMember().getVoiceState().getChannel();
					try {
						if (channel != null) {
							// set the user limit to the channel
							channel.getManager().setUserLimit(Integer.parseInt(command.replace("limit ", ""))).queue();
							valid = true;
						}
					} catch (NumberFormatException e) {

					} catch (IllegalArgumentException e1) {

					}
				}else if(command.equalsIgnoreCase("create text")) {
					// get the voice channel
					VoiceChannel channel = event.getMember().getVoiceState().getChannel();
					
					if(channel != null && !chatroomToTextroom.containsKey(channel)) {
						TextChannel tx = shlongshot.createTextChannel(channel.getName()).setParent(chatRoomsCat)
						.complete();
						chatroomToTextroom.put(channel, tx);
						
						// Hide the channel
						tx.createPermissionOverride(shlongshot.getPublicRole()).setDeny(Permission.VIEW_CHANNEL).queue();
						
						for(Member x : channel.getMembers()) {
							tx.createPermissionOverride(x).setAllow(Permission.VIEW_CHANNEL).queue();
						}
						
						
						valid = true;
					}
				}else if(command.equalsIgnoreCase("delete text")) {
					// get the voice channel
					VoiceChannel channel = event.getMember().getVoiceState().getChannel();
					
					if(channel != null && chatroomToTextroom.containsKey(channel)) {
						
						chatroomToTextroom.get(channel).delete().queue();
						chatroomToTextroom.remove(channel);
						
						valid = true;
					}
				}

				if (!valid) {
					// we get here if the command isnt valid. Lets send the dumb butt a message
					event.getAuthor().openPrivateChannel().complete()
							.sendMessage("Unknown or invalid command:" + event.getMessage().getContentDisplay())
							.queue();
				}

				// delete the message from the channel
				event.getMessage().delete().queue();
			}
		}
	}

	/**
	 * Deletes this channel if there are no players in it and it matches the chat
	 * room parent
	 * 
	 * @param leftChannel The channel the player left
	 */
	private void playerLeft(VoiceChannel leftChannel, Member user) {
		if (!ignoredChannelIDs.contains(leftChannel.getIdLong()) && leftChannel.getParent() != null) {
			
			// we need to remove them from being able to view the text channel if it exists
			if(chatroomToTextroom.containsKey(leftChannel)) {
				System.out.println("hm");
				try {
					chatroomToTextroom.get(leftChannel).putPermissionOverride(user).setDeny(Permission.VIEW_CHANNEL).queue();
				} catch(IllegalStateException e) {
					e.printStackTrace();
					System.out.println("we died");
				}
			}
			
			// we do this is there are 0 people left in the room
			if(leftChannel.getParent().equals(chatRoomsCat) && leftChannel.getMembers().size() <= 0) {
				// delete the channel
				leftChannel.delete().queue();
			
				if(chatroomToTextroom.containsKey(leftChannel)) {
					chatroomToTextroom.get(leftChannel).delete().queue();
					chatroomToTextroom.remove(leftChannel);
				}
			}
			
		}
	}

	/**
	 * If a user joins the create channel, create a room and move them to it
	 * 
	 * @param joinChannel The channel the player joined
	 * @param user        The player
	 */
	private void playerJoined(VoiceChannel joinChannel, Member user) {		
		// joined create channel
		if (createChatIDs.contains(joinChannel.getIdLong())) {
			int number = 1;
			while (shlongshot.getVoiceChannelsByName("Chatroom " + number, true).size() > 0) {
				number++;
			}
			VoiceChannel newChannel = shlongshot.createVoiceChannel("Chatroom " + number).setParent(chatRoomsCat)
					.complete();
			shlongshot.moveVoiceMember(user, newChannel).queue();
		}else if(joinChannel.getParent() == chatRoomsCat){
			//we get here if they join a chatroom and it isnt the create chatroom but is a created chatroom
			try {
				// we need to add them to being able to view the text channel if it exists
				if(chatroomToTextroom.containsKey(joinChannel)) {
					chatroomToTextroom.get(joinChannel).putPermissionOverride(user).setAllow(Permission.VIEW_CHANNEL).queue();
				}
			} catch(IllegalStateException e) {
				
			}
			
		}
	}
}
