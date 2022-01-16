package bot.role;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@ToString
public class Player implements Serializable {
	
	public static final int DAILY_CHALLENGE_LIMIT = 3;
	public static final int DAILY_DEFEND_LIMIT = 3;
	
	private static final long serialVersionUID = -431615875063733037L;
	
	private int strength, agility, knowledge, magic, stamina;
	private long gold;
	private int tournamentWins;
	private int wins,losses;
	private int challengedToday, hasChallengedToday;
	
	/**
	 * Can the user challenge another user
	 * @return true if they can challenge
	 */
	public boolean canChallenge() {
		return hasChallengedToday < DAILY_CHALLENGE_LIMIT;
	}
	
	/**
	 * Can the user be fought by another user
	 * @return true if they can be fought again
	 */
	public boolean canDefend() {
		return challengedToday < DAILY_DEFEND_LIMIT;
	}

}
