package partybot.listeners;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import data.ConfigLoader;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.restaction.CommandUpdateAction;
import partybot.dataStructures.PartyGuild;
import net.dv8tion.jda.api.requests.restaction.CommandUpdateAction.CommandData;
import net.dv8tion.jda.api.requests.restaction.CommandUpdateAction.OptionData;
import static net.dv8tion.jda.api.entities.Command.OptionType.*;

public class PartyRoomListener extends ListenerAdapter {

	private Logger logger = LoggerFactory.getLogger(PartyRoomListener.class);

	// PartyBot version
	public static final String VERSION = "1.1.1";

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
				CommandUpdateAction commands = event.getJDA().getGuildById(id).updateCommands();

				commands.addCommands(
						new CommandData("rename", "Renames the chatroom and any related text rooms to a new name")
								.addOption(new OptionData(STRING, "name", "Name to rename too").setRequired(true)));
				commands.addCommands(new CommandData("limit", "Limits the amount of people who can enter a chatroom")
								.addOption(new OptionData(INTEGER, "count", "Number of people allowed in the chatroom").setRequired(true)));
				commands.addCommands(new CommandData("create-text","Creates a text chatroom that only people in the voice channel can see"));
				commands.addCommands(new CommandData("delete-text","Deletes any associated text chat rooms tied to the voice channel"));
				commands.addCommands(new CommandData("breakout","Creates a new chatroom and moves all people playing the same game"));
				try {
					commands.complete();
				}catch(Exception e) {
					logger.error("We have done too many updates on commands for today");
				}
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
	 * Takes an incoming message event and handles it
	 * 
	 * @param event
	 */
	public void onGuildMessageRecieved(GuildMessageReceivedEvent event) {
		if (event.getMessage().getContentDisplay().startsWith("</") && event.getChannel() == partyGuilds.get(event.getGuild()).getCommandChannel()) {
			event.getMessage().delete().queue();
		}
		
		System.out.println(event.getMember().getActivities());
	}

	/**
	 * On voice chat leave
	 */
	@Override
	public void onGuildVoiceLeave(GuildVoiceLeaveEvent event) {
		playerLeft(event.getChannelLeft(), event.getMember(), event.getGuild());
	}

	@Override
	public void onSlashCommand(SlashCommandEvent event) {
		if (event.getGuild() == null) {
			return;
		}

		switch (event.getName()) {
		case "rename":
			rename(event);
			break;
		case "limit":
			limit(event);
			break;
		case "create-text":
			createText(event);
			break;
		case "delete-text":
			deleteText(event);
			break;
		case "breakout":
			breakoutRoom(event);
			break;
		default:
			event.reply("I do not know what that command is").setEphemeral(true).queue();

		}
	}
	
	private void breakoutRoom(SlashCommandEvent event) {
		event.acknowledge(true).queue();
		VoiceChannel channel = event.getMember().getVoiceState().getChannel();
		if(event.getMember().getActivities().size() > 0 && channel != null) {
			Activity activity = event.getMember().getActivities().get(0);
			VoiceChannel newVoiceChannel = event.getGuild().createVoiceChannel(activity.getName())
					.setParent(partyGuilds.get(event.getGuild()).getPartyChatroomCategory()).complete();
			for(Member x : channel.getMembers()) {
				if(x.getActivities().size() > 0 && x.getActivities().get(0).equals(activity)) {
					event.getGuild().moveVoiceMember(x, newVoiceChannel).queue();
				}
			}
		}
		
	}

	private void deleteText(SlashCommandEvent event) {
		event.acknowledge(true).queue();
		VoiceChannel channel = event.getMember().getVoiceState().getChannel();

		if (channel != null && partyGuilds.get(event.getGuild()).getChannelLinks().containsKey(channel)) {
			partyGuilds.get(event.getGuild()).getChannelLinks().get(channel).delete().queue();
			partyGuilds.get(event.getGuild()).getChannelLinks().remove(channel);
		}
	}

	private void createText(SlashCommandEvent event) {
		event.acknowledge(true).queue();
		VoiceChannel channel = event.getMember().getVoiceState().getChannel();

		if (channel != null && !partyGuilds.get(event.getGuild()).getChannelLinks().containsKey(channel)) {
			TextChannel tx = event.getGuild().createTextChannel(channel.getName())
					.setParent(partyGuilds.get(event.getGuild()).getPartyChatroomCategory()).complete();
			partyGuilds.get(event.getGuild()).getChannelLinks().put(channel, tx);

			// Hide the channel
			tx.createPermissionOverride(event.getGuild().getPublicRole()).setDeny(Permission.VIEW_CHANNEL).queue();

			for (Member x : channel.getMembers()) {
				tx.createPermissionOverride(x).setAllow(Permission.VIEW_CHANNEL).queue();
			}
		}

	}
	
	/**
	 * Renames chat room
	 * 
	 * @param event
	 */
	private void rename(SlashCommandEvent event) {
		event.acknowledge(true).queue();
		try {
			VoiceChannel channel = event.getMember().getVoiceState().getChannel();
			if (channel != null) {
				try {
					// rename the channel and set valid command to true
					channel.getManager().setName(event.getOption("name").getAsString()).queue();
					if (partyGuilds.get(event.getGuild()).getChannelLinks().containsKey(channel)) {
						partyGuilds.get(event.getGuild()).getChannelLinks().get(channel).getManager()
								.setName(event.getOption("name").getAsString()).queue();
					}
				} catch (IllegalArgumentException e1) {

				}
			}
		} catch (NullPointerException e) {

		}
	}
	
	private void limit(SlashCommandEvent event) {
		event.acknowledge(true).queue();
		try {
			VoiceChannel channel = event.getMember().getVoiceState().getChannel();
			if (channel != null) {
				try {
					channel.getManager().setUserLimit(Integer.parseInt(event.getOption("count").getAsString())).queue();
				} catch (IllegalArgumentException e1) {

				}
			}
		} catch (NullPointerException e) {

		}
	}

	/**
	 * Deletes this channel if there are no players in it and it matches the chat
	 * room parent
	 * 
	 * @param leftChannel The channel the player left
	 */
	private void playerLeft(VoiceChannel leftChannel, Member user, Guild guild) {
		if (partyGuilds.containsKey(guild) && !partyGuilds.get(guild).getIgnoredChannels().contains(leftChannel)
				&& leftChannel.getParent() != null) {

			// we need to remove them from being able to view the text channel if it exists
			if (partyGuilds.get(guild).getChannelLinks().containsKey(leftChannel)) {
				try {
					partyGuilds.get(guild).getChannelLinks().get(leftChannel).putPermissionOverride(user)
							.setDeny(Permission.VIEW_CHANNEL).queue();
				} catch (IllegalStateException e) {
					e.printStackTrace();
					System.out.println("we died");
				}
			}

			// we do this is there are 0 people left in the room
			if (leftChannel.getParent().equals(partyGuilds.get(guild).getPartyChatroomCategory())
					&& leftChannel.getMembers().size() <= 0) {
				// delete the channel
				leftChannel.delete().queue();

				if (partyGuilds.get(guild).getChannelLinks().containsKey(leftChannel)) {
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
			VoiceChannel newChannel = guild.createVoiceChannel("Chatroom " + number)
					.setParent(partyGuilds.get(guild).getPartyChatroomCategory()).complete();
			guild.moveVoiceMember(user, newChannel).queue();
		} else if (partyGuilds.containsKey(guild)
				&& joinChannel.getParent() == partyGuilds.get(guild).getPartyChatroomCategory()) {
			// we get here if they join a chatroom and it isnt the create chatroom but is a
			// created chatroom
			try {
				// we need to add them to being able to view the text channel if it exists
				if (partyGuilds.get(guild).getChannelLinks().containsKey(joinChannel)) {
					partyGuilds.get(guild).getChannelLinks().get(joinChannel).putPermissionOverride(user)
							.setAllow(Permission.VIEW_CHANNEL).queue();
				}
			} catch (IllegalStateException e) {

			}

		}
	}
}
