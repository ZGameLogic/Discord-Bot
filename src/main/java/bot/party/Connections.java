package bot.party;

import java.util.HashMap;

import data.serializing.SaveableData;

public class Connections extends SaveableData {
	private static final long serialVersionUID = 1585832051627414082L;
	private HashMap<Long, Long> idLinks;

	public Connections() {
		super("Channel Links");
		idLinks = new HashMap<>();
	}
	
	public boolean hasLink(long voiceId) {
		return idLinks.containsKey(voiceId);
	}
	
	public void addLink(long voiceId, long textId) {
		idLinks.put(voiceId, textId);
	}
	
	public void removeLink(long voiceId) {
		if(idLinks.containsKey(voiceId)) {
			idLinks.remove(voiceId);
		}
	}
	
	public long getLink(long voiceId) {
		return idLinks.get(voiceId);
	}
}