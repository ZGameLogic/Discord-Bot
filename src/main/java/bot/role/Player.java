package bot.role;

import bot.role.data.Item;
import bot.role.data.Item.StatType;
import data.SaveableData;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Player extends SaveableData {
	
	private static final long serialVersionUID = -431615875063733037L;
	
	private int strength, agility, knowledge, magic, stamina;
	private long gold;
	private int tournamentWins;
	private int wins,losses;
	private int challengedToday, hasChallengedToday;
	private Item item;
	private int daysSinceLastActive;
	
	
	
	/**
	 * Can the user challenge another user
	 * @return true if they can challenge
	 */
	public boolean canChallenge() {
		if(item != null && item.getItemType() == StatType.STATIC_MAX_ACTIVITIES) {
			return hasChallengedToday < RoleBotListener.dailyChallengeLimit + item.getStatIncrease();
		}
		return hasChallengedToday < RoleBotListener.dailyChallengeLimit;
	}
	
	public int getActivitiesLeft() {
		if(item != null && item.getItemType() == StatType.STATIC_MAX_ACTIVITIES) {
			return RoleBotListener.dailyChallengeLimit + item.getStatIncrease() - hasChallengedToday;
		}
		return RoleBotListener.dailyChallengeLimit - hasChallengedToday;
	}
	
	/**
	 * Can the user be fought by another user
	 * @return true if they can be fought again
	 */
	public boolean canDefend() {
		return challengedToday < RoleBotListener.dailyDefendLimit;
	}
	
	@SuppressWarnings("incomplete-switch")
	public String getCompactStats() {
		if(item != null && item.isStatic()) {
			String str = strength + "";
			String kno = knowledge + "";
			String mag = magic + "";
			String agi = agility + "";
			String sta = stamina + "";
			switch(item.getItemType()) {
			case STATIC_AGILITY:
				agi += "(+" + item.getStatIncrease() + ")";
				break;
			case STATIC_KNOWLEDGE:
				kno += "(+" + item.getStatIncrease() + ")";
				break;
			case STATIC_MAGIC:
				mag += "(+" + item.getStatIncrease() + ")";
				break;
			case STATIC_STAMINA:
				 sta += "(+" + item.getStatIncrease() + ")";
				break;
			case STATIC_STRENGTH:
				str += "(+" + item.getStatIncrease() + ")";
				break;			
			}
			return str + ":" + kno + ":" + mag + ":" + agi + ":" + sta + " " + gold + " " + wins + "/" + losses;
		}
		return strength + ":" + knowledge + ":" + magic + ":" + agility + ":" + stamina + " " + gold + " " + wins + "/" + losses;
	}
	
	public int getStrength() {
		if(item != null && item.getItemType() == StatType.STATIC_STRENGTH) {
			return strength + item.getStatIncrease();
		}
		return strength;
	}
	
	public int getKnowledge() {
		if(item != null && item.getItemType() == StatType.STATIC_KNOWLEDGE) {
			return knowledge + item.getStatIncrease();
		}
		return knowledge;
	}
	
	public int getStamina() {
		if(item != null && item.getItemType() == StatType.STATIC_STAMINA) {
			return stamina + item.getStatIncrease();
		}
		return stamina;
	}
	
	public int getAgility() {
		if(item != null && item.getItemType() == StatType.STATIC_AGILITY) {
			return agility + item.getStatIncrease();
		}
		return agility;
	}
	
	public int getMagic() {
		if(item != null && item.getItemType() == StatType.STATIC_MAGIC) {
			return magic + item.getStatIncrease();
		}
		return magic;
	}
	
	public int getRawStrength() {
		return strength;
	}
	
	public int getRawKnowledge() {
		return knowledge;
	}
	
	public int getRawStamina() {
		return stamina;
	}
	
	public int getRawAgility() {
		return agility;
	}
	
	public int getRawMagic() {
		return magic;
	}
	
	public void increaseStrength(int num) {
		strength += num;
	}
	
	public void increaseStamina(int num) {
		stamina += num;
	}
	
	public void increaseAgility(int num) {
		agility += num;
	}
	
	public void increaseMagic(int num) {
		magic += num;
	}

	public void increaseKnowledge(int num) {
		knowledge += num;
	}

	public void won() {
		wins++;
	}
	
	public void lost() {
		losses++;
	}
	
	public void hasChallenged() {
		daysSinceLastActive = 0;
		hasChallengedToday++;
	}
	
	public void wasChallenged() {
		challengedToday++;
	}
	
	public boolean isActive() {
		return daysSinceLastActive < 2;
	}
	
	public void newDay() {
		if(hasChallengedToday == 0) {
			daysSinceLastActive++;
		}
		challengedToday = hasChallengedToday = 0;
	}
	
	public void increaseGold(long amount) {
		gold += amount;
	}
	
	public int getIntGold() {
		return (int)gold;
	}
	
	public void decreaseGold(long amount) {
		gold -= amount;
	}
	
	public int getTotal() {
		return strength + agility + knowledge + magic + stamina;
	}

	public Player(EncounterPlayer ep) {
		super(ep.getEncounterID());
		strength = ep.getStrength();
		agility = ep.getAgility();
		knowledge = ep.getKnowledge();
		magic = ep.getKnowledge();
		stamina =  ep.getStamina();
	}

	/**
	 * @param id
	 * @param strength
	 * @param agility
	 * @param knowledge
	 * @param magic
	 * @param stamina
	 * @param gold
	 * @param tournamentWins
	 * @param wins
	 * @param losses
	 * @param challengedToday
	 * @param hasChallengedToday
	 * @param item
	 * @param daysSinceLastActive
	 */
	public Player(long id, int strength, int agility, int knowledge, int magic, int stamina, long gold,
			int tournamentWins, int wins, int losses, int challengedToday, int hasChallengedToday, Item item,
			int daysSinceLastActive) {
		super(id);
		this.strength = strength;
		this.agility = agility;
		this.knowledge = knowledge;
		this.magic = magic;
		this.stamina = stamina;
		this.gold = gold;
		this.tournamentWins = tournamentWins;
		this.wins = wins;
		this.losses = losses;
		this.challengedToday = challengedToday;
		this.hasChallengedToday = hasChallengedToday;
		this.item = item;
		this.daysSinceLastActive = daysSinceLastActive;
	}

}
