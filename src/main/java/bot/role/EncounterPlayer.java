package bot.role;

import java.io.Serializable;
import java.util.LinkedList;

import lombok.Getter;
import lombok.Setter;

@Getter
public class EncounterPlayer implements Serializable {

	private static final long serialVersionUID = 1459401891422720105L;

	private int strength, agility, knowledge, magic, stamina;
	@Setter
	private long encounterID;
	private LinkedList<Long> playersFought;
	private String name;
	private int daysOld;
	
	/**
	 * @param strength
	 * @param agility
	 * @param knowledge
	 * @param magic
	 * @param stamina
	 * @param encounterID
	 */
	public EncounterPlayer(int strength, int agility, int knowledge, int magic, int stamina, long encounterID, String name) {
		this.strength = strength;
		this.agility = agility;
		this.knowledge = knowledge;
		this.magic = magic;
		this.stamina = stamina;
		this.encounterID = encounterID;
		this.name = name;
		daysOld = 0;
		playersFought = new LinkedList<>();
	}
	
	public void addPlayerFought(long id) {
		playersFought.add(id);
	}
	
	public boolean canFightPlayer(long id) {
		return !playersFought.contains(id);
	}
	
	/**
	 * @return true if time to delete
	 */
	public boolean dayPassed() {
		daysOld++;
		if(daysOld > 4) {
			return true;
		}
		return false;
	}

}
