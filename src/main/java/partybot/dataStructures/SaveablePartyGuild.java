package partybot.dataStructures;

import java.io.Serializable;

public class SaveablePartyGuild implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private long commandChannelID;
	private long partyChatroomCategoryID;
	private long createVoiceChannelID;
	
	/**
	 * Create save-able party guild
	 * @param commandChannelID
	 * @param partyChatroomCategoryID
	 * @param createVoiceChannelID
	 */
	public SaveablePartyGuild(long commandChannelID, long partyChatroomCategoryID, long createVoiceChannelID) {
		this.commandChannelID = commandChannelID;
		this.partyChatroomCategoryID = partyChatroomCategoryID;
		this.createVoiceChannelID = createVoiceChannelID;
	}
	
	public long getCommandChannelID() {
		return commandChannelID;
	}
	
	public void setCommandChannelID(long commandChannelID) {
		this.commandChannelID = commandChannelID;
	}
	
	public long getPartyChatroomCategoryID() {
		return partyChatroomCategoryID;
	}
	
	public void setPartyChatroomCategoryID(long partyChatroomCategoryID) {
		this.partyChatroomCategoryID = partyChatroomCategoryID;
	}
	
	public long getCreateVoiceChannelID() {
		return createVoiceChannelID;
	}
	
	public void setCreateVoiceChannelID(long createVoiceCHannelID) {
		this.createVoiceChannelID = createVoiceCHannelID;
	}

}
