package bot.role;

import java.time.OffsetDateTime;
import java.util.LinkedList;

import bot.role.data.Item.StatType;
import data.serializing.SaveableData;
import lombok.Getter;
import lombok.Setter;

@Getter
public class EncounterPlayer extends SaveableData {

	private static final long serialVersionUID = 1459401891422720105L;

	private int strength, agility, knowledge, magic, stamina;
	@Setter
	private long encounterID;
	private LinkedList<Long> playersFought;
	private String name;
	private int daysOld;
	private StatType bane;
	
	@Setter
	private OffsetDateTime timeDepart;
	
	/**
	 * @param strength
	 * @param agility
	 * @param knowledge
	 * @param magic
	 * @param stamina
	 * @param encounterID
	 */
	public EncounterPlayer(long id, int strength, int agility, int knowledge, int magic, int stamina, long encounterID, String name, StatType bane, OffsetDateTime timeDepart) {
		super(id);
		this.strength = strength;
		this.agility = agility;
		this.knowledge = knowledge;
		this.magic = magic;
		this.stamina = stamina;
		this.encounterID = encounterID;
		this.name = name;
		daysOld = 0;
		playersFought = new LinkedList<>();
		this.bane = bane;
		this.timeDepart = timeDepart;
	}
	
	public int getTotal() {
		return strength + agility + knowledge + magic + stamina;
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
