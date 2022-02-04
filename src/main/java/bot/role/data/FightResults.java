package bot.role.data;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FightResults {
	
	private boolean attackerWon;
	private int attackerPoints;
	private int defenderPoints;
	private int strengthTotal, strengthRolled;
	private int magicTotal, magicRolled;
	private int agilityTotal, agilityRolled;
	private int knowledgeTotal, knowledgeRolled;
	private int staminaTotal, staminaRolled;
	private int defenderPadding, boosterPadding, paddingMultiplier;
	private int attackerStrength, defenderStrength;
	private int attackerStamina, defenderStamina;
	private int attackerMagic, defenderMagic;
	private int attackerAgility, defenderAgility;
	private int attackerKnowledge, defenderKnowledge;
	
	public String strengthString() {
		String percentage = String.format("%.2f%%", (attackerStrength / (double) strengthTotal) * 100);
		return "Strength (" + percentage + "): " + (strengthRolled <= attackerStrength ? "won" : "lost") + "\n"
				+ "\tAttacker: " + attackerStrength + "\tDefender: " + defenderStrength + "\n"
				+ "\tTotal: " + strengthTotal + "\tRolled: " + strengthRolled;
	}
	
	public String knowledgeString() {
		String percentage = String.format("%.2f%%", (attackerKnowledge / (double) knowledgeTotal) * 100);
		return "Knowledge ("  + percentage + "): " + (knowledgeRolled <= attackerKnowledge ? "won" : "lost") + "\n"
				+ "\tAttacker: " + attackerKnowledge + "\tDefender: " + defenderKnowledge + "\n"
				+ "\tTotal: " + knowledgeTotal + "\tRolled: " + knowledgeRolled;
	}
	
	public String staminaString() {
		String percentage = String.format("%.2f%%", (attackerStamina / (double) staminaTotal) * 100);
		return "Stamina (" + percentage + "): " + (staminaRolled <= attackerStamina ? "won" : "lost") + "\n"
				+ "\tAttacker: " + attackerStamina + "\tDefender: " + defenderStamina + "\n"
				+ "\tTotal: " + staminaTotal + "\tRolled: " + staminaRolled;
	}
	
	public String agilityString() {
		String percentage = String.format("%.2f%%", (attackerAgility / (double) agilityTotal) * 100);
		return "Agility (" + percentage + "): " + (agilityRolled <= attackerAgility ? "won" : "lost") + "\n"
				+ "\tAttacker: " + attackerAgility + "\tDefender: " + defenderAgility + "\n"
				+ "\tTotal: " + agilityTotal + "\tRolled: " + agilityRolled;
	}
	
	public String magicString() {
		String percentage = String.format("%.2f%%", (attackerMagic / (double) magicTotal) * 100);
		return "Magic ("  + percentage + "): " + (magicRolled <= attackerMagic ? "won" : "lost") + "\n"
				+ "\tAttacker: " + attackerMagic + "\tDefender: " + defenderMagic + "\n"
				+ "\tTotal: " + magicTotal + "\tRolled: " + magicRolled;
	}
	
	public String toString() {
		return "Attacker win: " + attackerWon +"\n"
				+ "Score: " + attackerPoints + " " + defenderPoints + "\n"
				+ "Booster padding: " + boosterPadding + "\n"
				+ "Padding Multiplier: x" + paddingMultiplier + "\n"
				+ "Defender padding level: " + defenderPadding + "\n"
				+ "Defender total stat padding: " + (defenderPadding * paddingMultiplier + boosterPadding) + "\n"
				+ strengthString() + "\n"
				+ staminaString() + "\n"
				+ agilityString() + "\n"
				+ knowledgeString() + "\n"
				+ magicString() + "\n";
	}

}
