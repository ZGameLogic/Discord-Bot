package bot.role;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FightResults {
	
	private boolean attackerWon;
	private int attackerPoints;
	private int defenderPoints;

}
