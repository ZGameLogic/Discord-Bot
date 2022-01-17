package bot.role;

import java.io.Serializable;
import java.util.LinkedList;

import lombok.Getter;

@Getter
public class EncounterPlayer implements Serializable {

	private static final long serialVersionUID = 1459401891422720105L;

	private int strength, agility, knowledge, magic, stamina;
	private long encounterID;
	private LinkedList<Long> playersFought;
	private String name;
	
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
		
		playersFought = new LinkedList<>();
	}
	
	public void addPlayerFought(long id) {
		playersFought.add(id);
	}
	
	public boolean canFightPlayer(long id) {
		return !playersFought.contains(id);
	}
	
	

}
