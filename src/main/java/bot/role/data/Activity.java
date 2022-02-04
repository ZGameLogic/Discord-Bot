package bot.role.data;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.LinkedList;
import java.util.Random;

import data.SaveableData;
import lombok.Getter;
import lombok.Setter;

@Getter
public class Activity extends SaveableData {

	private static final long serialVersionUID = -7376560594118671659L;
	
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

	private int actionCost;
	private int rewardAmount;
	private int goldCost;
	private ActivityReward reward;
	private LinkedList<Long> playersWorked;
	@Setter
	private OffsetDateTime timeDepart;
	
	/**
	 * @param actionCost
	 * @param rewardAmount
	 * @param reward
	 */
	public Activity(long id, int actionCost, int rewardAmount, int goldCost, ActivityReward reward, OffsetDateTime timeDepart) {
		super(id);
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
