package data.database.arena.player;

import java.util.Random;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import bot.role.RoleBotListener;
import data.database.arena.achievements.Achievements;
import data.database.arena.encounter.Encounter;
import data.database.arena.item.Item;
import data.database.arena.item.Item.StatType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Entity
@Table(name = "Player_Stats")
public class Player  {
	
    @Id
    @Column(name = "id")
    private Long id;
	private int strength, agility, knowledge, magic, stamina;
	private long gold;
	private int tournamentWins;
	private int wins,losses;
	private int challengedToday, hasChallengedToday;
	@Embedded
	@Column(nullable = true)
	private Item item;
	private int daysSinceLastActive;
	@Embedded
	private Achievements achievements;
	
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
		if(item != null) {
			if(item.getItemType() == StatType.ACTIVE_STRENGTH) {
				strength += item.getStatIncrease();
			}
		}
		strength += num;
	}
	
	public void increaseStamina(int num) {
		if(item != null) {
			if(item.getItemType() == StatType.ACTIVE_STAMINA) {
				stamina += item.getStatIncrease();
			}
		}
		stamina += num;
	}
	
	public void increaseAgility(int num) {
		if(item != null) {
			if(item.getItemType() == StatType.ACTIVE_AGILITY) {
				agility += item.getStatIncrease();
			}
		}
		agility += num;
	}
	
	public void increaseMagic(int num) {
		if(item != null) {
			if(item.getItemType() == StatType.ACTIVE_MAGIC) {
				magic += item.getStatIncrease();
			}
		}
		magic += num;
	}

	public void increaseKnowledge(int num) {
		if(item != null) {
			if(item.getItemType() == StatType.ACTIVE_KNOWLEDGE) {
				knowledge += item.getStatIncrease();
			}
		}
		knowledge += num;
	}

	public void won() {
		wins++;
		if(wins >= 10) {
			achievements.setBloodOnYourHands(true);
		}
		if(wins >= 100) {
			achievements.setRedLedger(true);
		}
		if(wins >= 200) {
			achievements.setItsJustForSport(true);
		}
		if(wins >= 1000) {
			achievements.setBetterThanThePlague(true);
		}
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
		if(item != null && item.getItemType() == StatType.ACTIVE_GOLD) {
			gold += new Random().nextInt(9) + 1;
		}
		gold += amount;
		if(gold >= 1000000) {
			achievements.setMillionare(true);
		}
		if(gold >= 1000) {
			achievements.setGoldenTouch(true);
		}
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

	public Player(Encounter ep) {
		strength = ep.getStrength();
		agility = ep.getAgility();
		knowledge = ep.getKnowledge();
		magic = ep.getKnowledge();
		stamina =  ep.getStamina();
	}

}
