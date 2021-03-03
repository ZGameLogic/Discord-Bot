package data;

import java.util.Arrays;
import java.util.LinkedList;

public class IDs {
	
	// IDs of chatrooms that should create chatrooms
	private final static LinkedList<Long> createChatIDs = new LinkedList<Long>(Arrays.asList(
			// Shlongshot
			812095961475317811l,
			// Test server
			812083428686168136l
			));
	
	// IDs of channels that should be ignored for deletion
	// AFKs channels
	// Create chatroom channels
	private final static LinkedList<Long> ignoredChannelIDs = new LinkedList<Long>(Arrays.asList(
			// Shlongshot afk channel
			371695546173358090l
			));
	
	private final static LinkedList<Long> textChannelIDs = new LinkedList<Long>(Arrays.asList(
			// Shlongshot command channel
			812909596175106048l,
			// Test server command channel
			812912306357928007l
			));
	
	
	public IDs() {
		// add all create chat channel IDs as these should be ignored
		ignoredChannelIDs.addAll(createChatIDs);
	}


	public static LinkedList<Long> getCreatechatids() {
		return createChatIDs;
	}


	public static LinkedList<Long> getIgnoredchannelids() {
		return ignoredChannelIDs;
	}


	public static LinkedList<Long> getTextchannelids() {
		return textChannelIDs;
	}
	
}
