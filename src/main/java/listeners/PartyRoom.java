package listeners;

import java.awt.Color;
import java.util.LinkedList;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class PartyRoom extends ListenerAdapter {
	
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
	
	public PartyRoom() {
		
		createChatIDs = new LinkedList<Long>();
		// Shlongshot chat ID
		createChatIDs.add(812095961475317811l);
		// Test server chat ID
		createChatIDs.add(812083428686168136l);

		ignoredChannelIDs = new LinkedList<Long>();
		// Ignore all create chat room IDs
		ignoredChannelIDs.addAll(createChatIDs);
		// AFK channel ID for Shlongshot
		ignoredChannelIDs.add(371695546173358090l);

		textChannelIDS = new LinkedList<Long>();
		// Shlongshot text ID
		textChannelIDS.add(812909596175106048l);
		// Test server text ID
		textChannelIDS.add(812912306357928007l);
	}

	/**
	 * Login event
	 */
	@Override
	public void onReady(ReadyEvent event) {
		for (Guild x : event.getJDA().getGuilds()) {
			if (x.getName().contains("MemeBot test server") || x.getName().contains("Shlongshot")) {
				System.out.println("Found guild");
				shlongshot = x;
				break;
			}
		}

		// Get the category (This is for determining if the chat room should be deleted when done)
		for (Category x : event.getJDA().getCategories()) {
			if (x.getName().contains("Chat Rooms")) {
				System.out.println("Found category");
				chatRoomsCat = x;
				break;
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

	@Override
	public void onPrivateMessageReceived(PrivateMessageReceivedEvent event) {
		if (!event.getAuthor().isBot()) {
			EmbedBuilder eb = new EmbedBuilder();

			eb.setTitle("Uh oh");
			eb.setColor(Color.magenta);
			eb.setDescription("My master hasn't programed this feature into me yet.");
			eb.addField("Heres a neat skyrim pic for ya", "Why yes, it is random each time", false);
			eb.setImage("http://zgamelogic.com/skyrim/image" + (int) ((Math.random() * 40) + 1) + ".jpg");

			MessageEmbed embed = eb.build();

			event.getAuthor().openPrivateChannel().complete().sendMessage(embed).complete();

			/*
			 * eb.setFooter("This is an edited in footer");
			 * sent.editMessage(eb.build()).queue();
			 */

		}
	}

	/**
	 * On voice chat join
	 */
	@Override
	public void onGuildVoiceJoin(GuildVoiceJoinEvent event) {
		System.out.println("Guild member joined");
		playerJoined(event.getChannelJoined(), event.getMember());
	}

	/**
	 * On voice chat move
	 */
	@Override
	public void onGuildVoiceMove(GuildVoiceMoveEvent event) {
		playerLeft(event.getChannelLeft());
		playerJoined(event.getChannelJoined(), event.getMember());
	}

	/**
	 * On voice chat leave
	 */
	@Override
	public void onGuildVoiceLeave(GuildVoiceLeaveEvent event) {
		playerLeft(event.getChannelLeft());
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
	private void playerLeft(VoiceChannel leftChannel) {
		if (!ignoredChannelIDs.contains(leftChannel.getIdLong()) && leftChannel.getParent() != null
				&& leftChannel.getParent().equals(chatRoomsCat) && leftChannel.getMembers().size() <= 0) {
			// delete the channel
			leftChannel.delete().queue();
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
		}
	}
}
