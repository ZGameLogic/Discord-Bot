package data.database.arena.encounter;

import java.time.OffsetDateTime;
import java.util.LinkedList;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import data.database.arena.item.Item.StatType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Encounters")
public class Encounter {

	private int strength, agility, knowledge, magic, stamina;
	@Id
	private long id;
	private LinkedList<Long> playersFought;
	private String name;
	private int daysOld;
	private StatType bane;
	private OffsetDateTime timeDepart;
	
	/**
	 * @param strength
	 * @param agility
	 * @param knowledge
	 * @param magic
	 * @param stamina
	 * @param encounterID
	 */
	public Encounter(long id, int strength, int agility, int knowledge, int magic, int stamina, String name, StatType bane, OffsetDateTime timeDepart) {
		this.id = id;
		this.strength = strength;
		this.agility = agility;
		this.knowledge = knowledge;
		this.magic = magic;
		this.stamina = stamina;
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
