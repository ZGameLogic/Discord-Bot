package bot.role.data;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.Random;

import lombok.Getter;

@Getter
public class Activity implements Serializable {

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
				return Gold;
			}
			return ActivityReward.values()[new Random().nextInt(ActivityReward.values().length)];
		}
	}

	private int actionCost;
	private int rewardAmount;
	private int goldCost;
	private ActivityReward reward;
	private LinkedList<Long> playersWorked;
	
	/**
	 * @param actionCost
	 * @param rewardAmount
	 * @param reward
	 */
	public Activity(int actionCost, int rewardAmount, int goldCost, ActivityReward reward) {
		this.actionCost = actionCost;
		this.rewardAmount = rewardAmount;
		this.reward = reward;
		this.goldCost = goldCost;
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
