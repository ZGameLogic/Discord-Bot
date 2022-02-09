package data.database.arena.activity;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.LinkedList;
import java.util.Random;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

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
@Table(name = "Activities")
public class Activity {
	
	public enum ActivityReward implements Serializable {
		Gold,
		Strength,
		Stamina,
		Magic,
		Knowledge,
		Agility;
		
		public static ActivityReward random() {
			Random r = new Random(System.currentTimeMillis());
			if(r.nextInt(3) == 1) {
				return ActivityReward.values()[new Random().nextInt(ActivityReward.values().length - 1) + 1];
			}
			return Gold;
		}
	}

	@Id
	private long id;
	private int actionCost;
	private int rewardAmount;
	private int goldCost;
	private ActivityReward reward;
	private LinkedList<Long> playersWorked;
	private OffsetDateTime timeDepart;
	
	public Activity(bot.role.data.Activity a) {
		id = a.getIdLong();
		actionCost = a.getActionCost();
		rewardAmount = a.getRewardAmount();
		goldCost = a.getGoldCost();
		playersWorked = a.getPlayersWorked();
		timeDepart = a.getTimeDepart();
		switch(a.getReward()) {
		case Agility:
			reward = ActivityReward.Agility;
			break;
		case Gold:
			reward = ActivityReward.Gold;
			break;
		case Knowledge:
			reward = ActivityReward.Knowledge;
			break;
		case Magic:
			reward = ActivityReward.Magic;
			break;
		case Stamina:
			reward = ActivityReward.Stamina;
			break;
		case Strength:
			reward = ActivityReward.Strength;
			break;
		
		}
	}
	
	/**
	 * @param actionCost
	 * @param rewardAmount
	 * @param reward
	 */
	public Activity(long id, int actionCost, int rewardAmount, int goldCost, ActivityReward reward, OffsetDateTime timeDepart) {
		this.id = id;
		this.actionCost = actionCost;
		this.rewardAmount = rewardAmount;
		this.reward = reward;
		this.goldCost = goldCost;
		this.timeDepart = timeDepart;
		playersWorked = new LinkedList<>();
	}
	
	/**
	 * Add the player id to the list of players who already complete this activity
	 * @param id
	 */
	public void addPlayerWorked(long id) {
		playersWorked.add(id);
	}
	
	/**
	 * Check to see if a player can work this activity
	 * @param id
	 * @return true if the player can work the activity
	 */
	public boolean canPlayerWork(long id) {
		return !playersWorked.contains(id);
	}

}
