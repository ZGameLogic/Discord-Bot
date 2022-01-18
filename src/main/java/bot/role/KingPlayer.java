package bot.role;

import java.io.Serializable;
import java.util.LinkedList;

import lombok.Getter;

public class KingPlayer implements Serializable {

	private static final long serialVersionUID = 2505140488810302204L;

	@Getter
	private LinkedList<Long> playersFought;
	
	public KingPlayer() {
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
