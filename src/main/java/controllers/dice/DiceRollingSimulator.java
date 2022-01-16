package controllers.dice;

public interface DiceRollingSimulator {

	public static long rollDice(long diceCount, long faceCount) {
		long total = 0;
		for(int i = 0; i < diceCount; i++) {
			total += (int)(Math.random() * faceCount) + 1;
		}
		return total;
	}
	
}
