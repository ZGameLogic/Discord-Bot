package partybot.listeners;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import data.ConfigLoader;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import partybot.dataStructures.PartyGuild;

public class PartyRoomListener extends ListenerAdapter {
	
	private Logger logger = LoggerFactory.getLogger(PartyRoomListener.class);
	
	//PartyBot version
	public static final String VERSION = "1.0.1";

	private Map<Guild, PartyGuild> partyGuilds;
	
	private ConfigLoader cl;
	
	public PartyRoomListener(ConfigLoader cl) {
		
		this.cl = cl;
		
		partyGuilds = new HashMap<Guild, PartyGuild>();
	}

	/**
	 * Login event
	 */
	@Override
	public void onReady(ReadyEvent event) {
		
		for (Long id : cl.getPartyGuildIDs()) {
			if (event.getJDA().getGuildById(id) != null) {
				partyGuilds.put(event.getJDA().getGuildById(id), new PartyGuild(event.getJDA().getGuildById(id)));
			}
		}
		
		logger.info("Party Room Listener started...");
	}

	/**
	 * When we get a message
	 */
	@Override
	public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
		onGuildMessageRecieved(event);
	}
	
	/**
	 * On voice chat join
	 */
	@Override
	public void onGuildVoiceJoin(GuildVoiceJoinEvent event) {
		playerJoined(event.getChannelJoined(), event.getMember(), event.getGuild());
	}

	/**
	 * On voice chat move
	 */
	@Override
	public void onGuildVoiceMove(GuildVoiceMoveEvent event) {
		playerLeft(event.getChannelLeft(), event.getMember(), event.getGuild());
		playerJoined(event.getChannelJoined(), event.getMember(), event.getGuild());
	}

	/**
	 * On voice chat leave
	 */
	@Override
	public void onGuildVoiceLeave(GuildVoiceLeaveEvent event) {
		playerLeft(event.getChannelLeft(), event.getMember(), event.getGuild());
	}

	/**
	 * Takes an incoming message event and handles it
	 * 
	 * @param event
	 */
	private void onGuildMessageRecieved(GuildMessageReceivedEvent event) {
		if (!event.getAuthor().isBot() && partyGuilds.containsKey(event.getGuild())) {
			if (partyGuilds.get(event.getGuild()).getCommandChannel() == event.getChannel()) {

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
							if(partyGuilds.get(event.getGuild()).getChannelLinks().containsKey(channel)) {
								partyGuilds.get(event.getGuild()).getChannelLinks().get(channel).getManager().setName(command.replace("rename ", "")).queue();
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
					
					if(channel != null && !partyGuilds.get(event.getGuild()).getChannelLinks().containsKey(channel)) {
						TextChannel tx = event.getGuild().createTextChannel(channel.getName()).setParent(partyGuilds.get(event.getGuild()).getPartyChatroomCategory())
						.complete();
						partyGuilds.get(event.getGuild()).getChannelLinks().put(channel, tx);
						
						// Hide the channel
						tx.createPermissionOverride(event.getGuild().getPublicRole()).setDeny(Permission.VIEW_CHANNEL).queue();
						
						for(Member x : channel.getMembers()) {
							tx.createPermissionOverride(x).setAllow(Permission.VIEW_CHANNEL).queue();
						}
						
						
						valid = true;
					}
				}else if(command.equalsIgnoreCase("delete text")) {
					// get the voice channel
					VoiceChannel channel = event.getMember().getVoiceState().getChannel();
					
					if(channel != null && partyGuilds.get(event.getGuild()).getChannelLinks().containsKey(channel)) {
						
						partyGuilds.get(event.getGuild()).getChannelLinks().get(channel).delete().queue();
						partyGuilds.get(event.getGuild()).getChannelLinks().remove(channel);
						
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
	private void playerLeft(VoiceChannel leftChannel, Member user, Guild guild) {
		if (partyGuilds.containsKey(guild) && !partyGuilds.get(guild).getIgnoredChannels().contains(leftChannel) && leftChannel.getParent() != null) {
			
			// we need to remove them from being able to view the text channel if it exists
			if(partyGuilds.get(guild).getChannelLinks().containsKey(leftChannel)) {
				try {
					partyGuilds.get(guild).getChannelLinks().get(leftChannel).putPermissionOverride(user).setDeny(Permission.VIEW_CHANNEL).queue();
				} catch(IllegalStateException e) {
					e.printStackTrace();
					System.out.println("we died");
				}
			}
			
			// we do this is there are 0 people left in the room
			if(leftChannel.getParent().equals(partyGuilds.get(guild).getPartyChatroomCategory()) && leftChannel.getMembers().size() <= 0) {
				// delete the channel
				leftChannel.delete().queue();
			
				if(partyGuilds.get(guild).getChannelLinks().containsKey(leftChannel)) {
					partyGuilds.get(guild).getChannelLinks().get(leftChannel).delete().queue();
					partyGuilds.get(guild).getChannelLinks().remove(leftChannel);
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
	private void playerJoined(VoiceChannel joinChannel, Member user, Guild guild) {		
		// joined create channel
		if (partyGuilds.containsKey(guild) && partyGuilds.get(guild).getCreateRoom() == joinChannel) {
			int number = 1;
			while (guild.getVoiceChannelsByName("Chatroom " + number, true).size() > 0) {
				number++;
			}
			VoiceChannel newChannel = guild.createVoiceChannel("Chatroom " + number).setParent(partyGuilds.get(guild).getPartyChatroomCategory())
					.complete();
			guild.moveVoiceMember(user, newChannel).queue();
		}else if(partyGuilds.containsKey(guild) && joinChannel.getParent() == partyGuilds.get(guild).getPartyChatroomCategory()){
			//we get here if they join a chatroom and it isnt the create chatroom but is a created chatroom
			try {
				// we need to add them to being able to view the text channel if it exists
				if(partyGuilds.get(guild).getChannelLinks().containsKey(joinChannel)) {
					partyGuilds.get(guild).getChannelLinks().get(joinChannel).putPermissionOverride(user).setAllow(Permission.VIEW_CHANNEL).queue();
				}
			} catch(IllegalStateException e) {
				
			}
			
		}
	}
}
