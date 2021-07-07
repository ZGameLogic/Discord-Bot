package partybot.listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import setup.data.Guild.GuildType;
import setup.listeners.SetupListener;
import net.dv8tion.jda.api.interactions.commands.OptionType;
/**
 * 
 * @author Ben Shabowski
 *
 */
public class PartyRoomListener extends ListenerAdapter {

	private Logger logger = LoggerFactory.getLogger(PartyRoomListener.class);

	// PartyBot version
	public static final String VERSION = "1.1.1";

	//private Map<Guild, PartyGuild> partyGuilds;

	private SetupListener sl;
	
	public static final String[] chatroomNames = {"Chatroom", "Hangout", "Chillin", "Its bedtime"};

	public PartyRoomListener(SetupListener sl) {

		this.sl = sl;

		//partyGuilds = new HashMap<Guild, PartyGuild>();
	}

	/**
	 * Login event
	 */
	@Override
	public void onReady(ReadyEvent event) {

		for (String id : sl.getGuildIDs(GuildType.Party)) {
			if (event.getJDA().getGuildById(id) != null) {
				CommandListUpdateAction commands = event.getJDA().getGuildById(id).updateCommands();

				commands.addCommands(
						new CommandData("rename", "Renames the chatroom and any related text rooms to a new name")
								.addOptions(new OptionData(OptionType.STRING, "name", "Name to rename too").setRequired(true)));
				commands.addCommands(new CommandData("limit", "Limits the amount of people who can enter a chatroom")
								.addOptions(new OptionData(OptionType.INTEGER, "count", "Number of people allowed in the chatroom").setRequired(true)));
				commands.addCommands(new CommandData("create-text","Creates a text chatroom that only people in the voice channel can see"));
				commands.addCommands(new CommandData("delete-text","Deletes any associated text chat rooms tied to the voice channel"));
				try {
					commands.submit();
					commands.complete();
				}catch(Exception e) {
					logger.error("We have done too many updates on commands for today");
				}
				
				// Update party guild ignored channels
				try {
					sl.getGuildById(id).getIgnoredChannelList().add(event.getJDA().getGuildById(id).getAfkChannel());
				} catch(NullPointerException e) {
					
				}
				try {
					sl.getGuildById(id).getIgnoredChannelList().add(event.getJDA().getGuildById(id).getVoiceChannelById(sl.getGuildById(id).getCreateVoiceChannelID()));
				} catch(NullPointerException e) {
					
				}
				
			}
		}

		logger.info("Party Room Listener started...");
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
		VoiceChannel channel = event.getMember().getVoiceState().getChannel();
		String activity = "";
		if(event.getMember().getActivities().size() > 0 && channel != null) {
			try {
				for(Activity x : event.getMember().getActivities()) {
					activity = x.getName();
				}
			} catch (IllegalArgumentException e) {
				logger.error("User has invalid activity for breakout");
			}
			if(activity.equals("")) {
				event.reply("You must be playing a game to use this command").queue();
				return;
			}
			
			Category cat = event.getGuild().getCategoryById(sl.getGuildById(event.getGuild().getId()).getPartyChatroomCategoryID());
			
			VoiceChannel newVoiceChannel = event.getGuild().createVoiceChannel(activity).setParent(cat).complete();
			for(Member x : channel.getMembers()) {
					for(Activity y : x.getActivities()) {
						try {
							if(y.getName().equals(activity)) {
								event.getGuild().moveVoiceMember(x, newVoiceChannel).queue();
								break;
							}
						} catch (IllegalArgumentException e) {
							logger.error("User has invalid activity for breakout");
						}
					}
			}
		}
		
		event.reply("").queue();
	}

	private void createText(SlashCommandEvent event) {
		event.reply("Created text channel").queue();
		Category cat = event.getGuild().getCategoryById(sl.getGuildById(event.getGuild().getId()).getPartyChatroomCategoryID());
		VoiceChannel channel = event.getMember().getVoiceState().getChannel();

		if (channel != null && !sl.getGuildById(event.getGuild().getId()).getChannelLinks().containsKey(channel)) {
			TextChannel tx = event.getGuild().createTextChannel(channel.getName())
					.setParent(cat).complete();
			sl.getGuildById(event.getGuild().getId()).getChannelLinks().put(channel, tx);

			// Hide the channel
			tx.createPermissionOverride(event.getGuild().getPublicRole()).setDeny(Permission.VIEW_CHANNEL).queue();

			for (Member x : channel.getMembers()) {
				tx.createPermissionOverride(x).setAllow(Permission.VIEW_CHANNEL).queue();
			}
		}

	}
	
	private void deleteText(SlashCommandEvent event) {
		event.reply("Deleted text channel").queue();
		VoiceChannel channel = event.getMember().getVoiceState().getChannel();

		if (channel != null && sl.getGuildById(event.getGuild().getId()).getChannelLinks().containsKey(channel)) {
			sl.getGuildById(event.getGuild().getId()).getChannelLinks().get(channel).delete().queue();
			sl.getGuildById(event.getGuild().getId()).getChannelLinks().remove(channel);
		}
	}
	
	/**
	 * Renames chat room
	 * 
	 * @param event
	 */
	private void rename(SlashCommandEvent event) {
		event.reply("Renamed chatroom to " + event.getOption("name").getAsString()).queue();
		try {
			VoiceChannel channel = event.getMember().getVoiceState().getChannel();
			if (channel != null) {
				try {
					// rename the channel and set valid command to true
					channel.getManager().setName(event.getOption("name").getAsString()).queue();
					if (sl.getGuildById(event.getGuild().getId()).getChannelLinks().containsKey(channel)) {
						sl.getGuildById(event.getGuild().getId()).getChannelLinks().get(channel).getManager()
								.setName(event.getOption("name").getAsString()).queue();
					}
				} catch (IllegalArgumentException e1) {

				}
			}
		} catch (NullPointerException e) {

		}
	}
	
	private void limit(SlashCommandEvent event) {
		event.reply("Limited chatroom to " + event.getOption("count").getAsString() + " user(s)").queue();
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
		if (sl.getGuildIDs(GuildType.Party).contains(guild.getId()) && 
				!sl.getGuildById(
						guild.getId())
				.getIgnoredChannelList()
				.contains(leftChannel) && 
				leftChannel.getParent() != null) {

			// we need to remove them from being able to view the text channel if it exists
			if (sl.getGuildById(guild.getId()).getChannelLinks().containsKey(leftChannel)) {
				try {
					sl.getGuildById(guild.getId()).getChannelLinks().get(leftChannel).putPermissionOverride(user)
							.setDeny(Permission.VIEW_CHANNEL).queue();
				} catch (IllegalStateException e) {
					e.printStackTrace();
					logger.error("we died");
				}
			}

			// we do this is there are 0 people left in the room
			if (leftChannel.getParent().equals(guild.getCategoryById(sl.getGuildById(guild.getId()).getPartyChatroomCategoryID()))
					&& leftChannel.getMembers().size() <= 0) {
				// delete the channel
				leftChannel.delete().queue();

				if (sl.getGuildById(guild.getId()).getChannelLinks().containsKey(leftChannel)) {
					sl.getGuildById(guild.getId()).getChannelLinks().get(leftChannel).delete().queue();
					sl.getGuildById(guild.getId()).getChannelLinks().remove(leftChannel);
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
		if (sl.getGuildIDs(GuildType.Party).contains(guild.getId()) && sl.getGuildById(guild.getId()).getCreateVoiceChannelID().equals(joinChannel.getId())) {
			int number = 1;
			String chatroomName = chatroomNames[(int)(Math.random() * chatroomNames.length)];
			while (guild.getVoiceChannelsByName(chatroomName + " " + number, true).size() > 0) {
				number++;
			}
			VoiceChannel newChannel = guild.createVoiceChannel(chatroomName + " " + number)
					.setParent(guild.getCategoryById(sl.getGuildById(guild.getId()).getPartyChatroomCategoryID())).complete();
			guild.moveVoiceMember(user, newChannel).queue();
		} else if (sl.getGuildIDs(GuildType.Party).contains(guild.getId())
				&& joinChannel.getParent() == guild.getCategoryById(sl.getGuildById(guild.getId()).getPartyChatroomCategoryID())) {
			// we get here if they join a chatroom and it isnt the create chatroom but is a
			// created chatroom
			try {
				// we need to add them to being able to view the text channel if it exists
				if (sl.getGuildById(guild.getId()).getChannelLinks().containsKey(joinChannel)) {
					 sl.getGuildById(guild.getId()).getChannelLinks().get(joinChannel).putPermissionOverride(user)
							.setAllow(Permission.VIEW_CHANNEL).queue();
				}
			} catch (IllegalStateException e) {

			}
		}
	}
}
