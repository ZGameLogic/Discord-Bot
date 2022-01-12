package setup.data;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import data.DataCacher;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import setup.listeners.SetupListener;

public class Guild implements Serializable {
	
	private static final long serialVersionUID = 1L;

	public enum GuildType{
		Party,
		Code,
		Event,
		Music,
		Meme,
		API;
	}
	
	private LinkedList<GuildType> guildType;
	private String guildID;
	private String createGuildMessageID;
	private String guildOwnerID;
	
	private String APIToken;
	private String eventChannelID;
	
	// Category all chatrooms will be created in
	private String partyChatroomCategoryID;
	// Voice channel that users join
	private String createVoiceChannelID;
	// Links between the voice channels and their text channels
	transient private Map<VoiceChannel, TextChannel> channelLinks;
	// Ignored channels to not delete
	transient private LinkedList<VoiceChannel> ignoredChannels;	
	
	public Guild(String guildID, String createGuildMessageID, String guildOwnerID) {
		this.guildID = guildID;
		this.createGuildMessageID = createGuildMessageID;
		this.guildOwnerID = guildOwnerID;
		guildType = new LinkedList<GuildType>();
		channelLinks = new HashMap<VoiceChannel, TextChannel>();
		ignoredChannels = new LinkedList<VoiceChannel>();
		save();
	}
	
	public Guild(File guildFile) {
		Guild loaded = (Guild) DataCacher.loadSerialized(guildFile.getPath());
		guildID = loaded.getGuildID();
		createGuildMessageID = loaded.getCreateGuildMessageID();
		guildOwnerID = loaded.getGuildOwnerID();
		
		APIToken = loaded.getAPIToken();
		
		eventChannelID = loaded.getEventChannelID();
		
		guildType = loaded.getGuildType();
		
		partyChatroomCategoryID = loaded.getPartyChatroomCategoryID();
		createVoiceChannelID = loaded.getCreateVoiceChannelID();
		
		channelLinks = new HashMap<VoiceChannel, TextChannel>();
		ignoredChannels = new LinkedList<VoiceChannel>();
	}

	public void addType(GuildType gt) {
		guildType.add(gt);
		save();
	}
	
	public void removeType(GuildType gt) {
		guildType.remove(gt);
		save();
	}
	
	public boolean isType(GuildType gt) {
		return guildType.contains(gt);
	}
	
	public void setupParty(net.dv8tion.jda.api.entities.Guild guild) {
		
		Category cat = guild.createCategory("Chat rooms").complete();
		VoiceChannel voice = guild.createVoiceChannel("Create chatroom", cat).complete();

		partyChatroomCategoryID = cat.getId();
		createVoiceChannelID = voice.getId();
		
		ignoredChannels = new LinkedList<VoiceChannel>();
		ignoredChannels.add(voice);

		save();
		
		CommandListUpdateAction commands = guild.updateCommands();

		commands.addCommands(
				new CommandData("rename", "Renames the chatroom and any related text rooms to a new name")
						.addOptions(new OptionData(OptionType.STRING, "name", "Name to rename too").setRequired(true)));
		commands.addCommands(new CommandData("limit", "Limits the amount of people who can enter a chatroom")
						.addOptions(new OptionData(OptionType.INTEGER, "count", "Number of people allowed in the chatroom").setRequired(true)));
		commands.addCommands(new CommandData("create-text","Creates a text chatroom that only people in the voice channel can see"));
		commands.addCommands(new CommandData("delete-text","Deletes any associated text chat rooms tied to the voice channel"));
		commands.addCommands(new CommandData("breakout","Creates a new chatroom and moves all people playing the same game"));
		try {
			commands.complete();
		}catch(Exception e) {
			
		}
	}

	public LinkedList<GuildType> getGuildType() {
		return guildType;
	}
	
	public LinkedList<VoiceChannel> getIgnoredChannelList(){
		return ignoredChannels;
	}
	
	public Map<VoiceChannel, TextChannel> getChannelLinks(){
		return channelLinks;
	}

	public String getGuildID() {
		return guildID;
	}

	public String getEventChannelID() {
		return eventChannelID;
	}

	public String getCreateGuildMessageID() {
		return createGuildMessageID;
	}

	public String getGuildOwnerID() {
		return guildOwnerID;
	}

	public String getAPIToken() {
		return APIToken;
	}

	public String getPartyChatroomCategoryID() {
		return partyChatroomCategoryID;
	}

	public String getCreateVoiceChannelID() {
		return createVoiceChannelID;
	}

	public void setCreateVoiceChannelID(String createVoiceChannelID) {
		this.createVoiceChannelID = createVoiceChannelID;
		save();
	}
	
	public void setPartyChatroomCategoryID(String partyChatroomCategoryID) {
		this.partyChatroomCategoryID = partyChatroomCategoryID;
		save();
	}

	public void setAPIToken(String aPIToken) {
		APIToken = aPIToken;
		save();
	}
	
	public void setEventChannelID(String eventChannelID) {
		this.eventChannelID = eventChannelID;
		save();
	}

	public void setCreateGuildMessageID(String createGuildMessageID) {
		this.createGuildMessageID = createGuildMessageID;
		save();
	}
	
	private void save() {
		DataCacher.saveSerialized(this, SetupListener.GUILD_DIR + "\\" + guildID + ".dat");
	}

}
