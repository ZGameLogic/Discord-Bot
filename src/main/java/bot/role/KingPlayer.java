package bot.role;

import java.util.LinkedList;

import data.SaveableData;
import lombok.Getter;

public class KingPlayer extends SaveableData {

	private static final long serialVersionUID = 2505140488810302204L;

	@Getter
	private LinkedList<Long> playersFought;
	
	public KingPlayer(String id) {
		super(id);
		playersFought = new LinkedList<>();
	}
	
	public void addPlayer(long id) {
		playersFought.add(id);
	}
	
	public void resetList() {
		playersFought = new LinkedList<>();
	}
	
	public boolean canFight(long id) {
		return !playersFought.contains(id);
	}
}
