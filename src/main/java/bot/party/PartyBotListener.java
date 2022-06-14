package bot.party;

import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import data.ConfigLoader;
import net.dv8tion.jda.api.entities.AudioChannel;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.ReconnectedEvent;
import net.dv8tion.jda.api.events.ResumedEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;

public class PartyBotListener extends ListenerAdapter {

	private Logger logger = LoggerFactory.getLogger(PartyBotListener.class);

	// Linked List of names to hold chatroom names
	private final LinkedList<String> chatroomNames;

	private final long chatroomCatID;
	private final long createChatID;
	private final long guildID;
	private final long AFKID;

	public PartyBotListener(ConfigLoader config) {

		chatroomCatID = config.getChatroomCatID();
		createChatID = config.getCreateChatID();
		guildID = config.getGuildID();
		AFKID = config.getAFKID();

		chatroomNames = new LinkedList<>();
		chatroomNames.add("Chatroom");
		chatroomNames.add("Hangout");
		chatroomNames.add("Chillin");
		chatroomNames.add("Roomchat");
		
	}

	/**
	 * Login event
	 */
	@Override
	public void onReady(ReadyEvent event) {
		logger.info("Party bot listener activated");
		checkCreateChatroom(event.getJDA().getGuildById(guildID).getVoiceChannelById(createChatID),
				event.getJDA().getGuildById(guildID));
	}

	@Override
	public void onReconnected(ReconnectedEvent event) {
		logger.info("Party bot listener re-activated");
		checkCreateChatroom(event.getJDA().getGuildById(guildID).getVoiceChannelById(createChatID),
				event.getJDA().getGuildById(guildID));
	}

	@Override
	public void onResumed(ResumedEvent event) {
		logger.info("Party bot listener re-activated");
		checkCreateChatroom(event.getJDA().getGuildById(guildID).getVoiceChannelById(createChatID),
				event.getJDA().getGuildById(guildID));
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
	 * Renames the voice channel Also renames any connected text channels
	 * 
	 * @param event
	 */
	public void renameChannel(SlashCommandInteraction event) {
		try {
			// get voice channel the user is is
			AudioChannel voice = event.getMember().getVoiceState().getChannel();
			if (event.getGuild().getVoiceChannelById(voice.getIdLong()).getParentCategoryIdLong() == chatroomCatID) {
				try {
					// rename the channel and set valid command to true
					voice.getManager().setName(event.getOption("name").getAsString()).queue();
					event.reply("Channel name updated").setEphemeral(true).queue();
					logger.info("Renamed chatroom");
				} catch (IllegalArgumentException e1) {
					event.reply("Channel name not updated").setEphemeral(true).queue();
				}
			} else {
				event.reply("You are not in a voice channel that supports this command").setEphemeral(true).queue();
			}
		} catch (NullPointerException e) {
			event.reply("You are not in a voice channel that supports this command").setEphemeral(true).queue();
		}
	}
	
	public void limit(SlashCommandInteraction event) {
		try {
			// get voice channel the user is is
			AudioChannel voice = event.getMember().getVoiceState().getChannel();
			if (event.getGuild().getVoiceChannelById(voice.getIdLong()).getParentCategoryIdLong() == chatroomCatID) {
				try {
					int limit = Integer.parseInt(event.getOption("count").getAsString());
					event.getGuild().getVoiceChannelById(voice.getIdLong()).getManager().setUserLimit(limit).queue();
					event.reply("Channel limit updated").setEphemeral(true).queue();
					logger.info("Limitted chatroom");
				} catch (IllegalArgumentException e1) {
					event.reply("Channel limit not updated").setEphemeral(true).queue();
				}
			} else {
				event.reply("You are not in a voice channel that supports this command").setEphemeral(true).queue();
			}
		} catch (NullPointerException e) {
			event.reply("You are not in a voice channel that supports this command").setEphemeral(true).queue();
		}
	}

	/**
	 * Deletes this channel if there are no players in it and it matches the chat
	 * category
	 * 
	 * @param channelLeft The channel the user left
	 * @param member      the user that left
	 * @param guild       the guild that the user left from
	 */
	private void playerLeft(AudioChannel channelLeft, Member member, Guild guild) {
		VoiceChannel channel = guild.getVoiceChannelById(channelLeft.getIdLong());
		if (channel.getParentCategoryIdLong() == chatroomCatID) {
			if (channel.getIdLong() != createChatID && channel.getIdLong() != AFKID) {
				// We get here if the channel left in is the chatroom categories
				if (channel.getMembers().size() == 0) {
					channel.delete().queue();
				}
			}
		}
	}

	/**
	 * If the user joins the create channel, create a room and move them to it
	 * 
	 * @param channelJoined the channel the user joined
	 * @param member        the user
	 * @param guild         the guild that this took place in
	 */
	private void playerJoined(AudioChannel channelJoined, Member member, Guild guild) {
		VoiceChannel channel = guild.getVoiceChannelById(channelJoined.getIdLong());
		if (channel.getParentCategoryIdLong() == chatroomCatID) {
			if (channel.getIdLong() == createChatID) {
				checkCreateChatroom(channel, guild);
			}
		}
	}

	/**
	 * Called when the bot is turned on
	 * 
	 * @param voiceChannel
	 */
	private void checkCreateChatroom(VoiceChannel voiceChannel, Guild guild) {
		List<Member> members = voiceChannel.getMembers();
		if (members.size() > 0) {
			int number = 1;
			String chatroomName = chatroomNames.get((int) (Math.random() * chatroomNames.size()));
			while (guild.getVoiceChannelsByName(chatroomName + " " + number, true).size() > 0) {
				number++;
			}
			VoiceChannel newChannel = guild.createVoiceChannel(chatroomName + " " + number)
					.setParent(guild.getCategoryById(chatroomCatID)).complete();
			logger.info("Created chatroom");
			for (Member member : members) {
				guild.moveVoiceMember(member, newChannel).queue();
			}
		}
	}
}
