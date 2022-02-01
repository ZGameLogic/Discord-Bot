package bot.role.data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Random;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
public class Item implements Serializable {

	private static final long serialVersionUID = 1667909803752847112L;

	public enum StatType implements Serializable {
		STATIC_STRENGTH("Increases Strength by: "),
		STATIC_MAGIC("Increases Magic by: "),
		STATIC_KNOWLEDGE("Increases Knowledge by: "),
		STATIC_AGILITY("Increases Agility by: "),
		STATIC_STAMINA("Increases Stamina by: "),
		STATIC_MAX_ACTIVITIES("Increases Activites by: "),
		
		ACTIVE_STRENGTH("Increases Strength gain by: "),
		ACTIVE_MAGIC("Increases Magic gain by: "),
		ACTIVE_KNOWLEDGE("Increases Knowledge gain by: "),
		ACTIVE_AGILITY("Increases Agility gain by: "),
		ACTIVE_STAMINA("Increases Stamina gain by: "),
		ACTIVE_GOLD("You always seem to find a bit more gold"),
		
		BANE_BANDIT("Wielder wins against any Bandit"),
		BANE_BLOB("Wielder wins against any Blob"),
		BANE_WIZARD("Wielder wins against any Wizard"),
		BANE_SKELETON("Wielder wins against any Skeleton"),
		BANE_WOLF("Wielder wins against any Wolf"),
		BANE_GHOUL("Wielder wins against any Ghoul"),
		BANE_GIANT("Wielder wins against any Giant"),
		BANE_TROLL("Wielder wins against any Troll");
		
		@Getter
		private String statDescription;
		
		private StatType(String statDesc) {
			statDescription = statDesc;
		}
		
		public static StatType getRandomMythic() {
			int choice = new Random().nextInt(15);
			return StatType.values()[choice + 6];
		}
		
		public static StatType getRandomStatic() {
			int choice = new Random().nextInt(5);
			return StatType.values()[choice];
		}
		
		public static HashMap<String, String> randomName(StatType type) {
			HashMap<String, String> possibleNames = new HashMap<>();
			switch(type) {
			case ACTIVE_AGILITY:
				possibleNames.put("", "");
				possibleNames.put("", "");
				possibleNames.put("", "");
				possibleNames.put("", "");
				possibleNames.put("", "");
				break;
			case ACTIVE_GOLD:
				possibleNames.put("Coin purse", "");
				possibleNames.put("", "");
				possibleNames.put("", "");
				possibleNames.put("", "");
				possibleNames.put("", "");
				break;
			case ACTIVE_KNOWLEDGE:
				possibleNames.put("Scroll of Java", "Ancient script that gives the knowledge of manipulation.");
				possibleNames.put("All seeing Spectacles", "Confortable, and fashionable. ");
				possibleNames.put("", "");
				possibleNames.put("", "");
				possibleNames.put("", "");
				break;
			case ACTIVE_MAGIC:
				possibleNames.put("Mirror Ball", "");
				possibleNames.put("", "");
				possibleNames.put("", "");
				possibleNames.put("", "");
				possibleNames.put("", "");
				break;
			case ACTIVE_STAMINA:
				possibleNames.put("", "");
				possibleNames.put("", "");
				possibleNames.put("", "");
				possibleNames.put("", "");
				possibleNames.put("", "");
				break;
			case ACTIVE_STRENGTH:
				possibleNames.put("", "");
				possibleNames.put("", "");
				possibleNames.put("", "");
				possibleNames.put("", "");
				possibleNames.put("", "");
				break;
			case BANE_BANDIT:
				possibleNames.put("Net Trap", "");
				possibleNames.put("", "");
				possibleNames.put("", "");
				possibleNames.put("", "");
				possibleNames.put("", "");
				break;
			case BANE_BLOB:
				possibleNames.put("Acid Bucket", "");
				possibleNames.put("", "");
				possibleNames.put("", "");
				possibleNames.put("", "");
				possibleNames.put("", "");
				break;
			case BANE_GHOUL:
				possibleNames.put("Flash Bomb", "");
				possibleNames.put("", "");
				possibleNames.put("", "");
				possibleNames.put("", "");
				possibleNames.put("", "");
				break;
			case BANE_GIANT:
				possibleNames.put("Trip Wire", "");
				possibleNames.put("", "");
				possibleNames.put("", "");
				possibleNames.put("", "");
				possibleNames.put("", "");
				break;
			case BANE_SKELETON:
				possibleNames.put("", "");
				possibleNames.put("", "");
				possibleNames.put("", "");
				possibleNames.put("", "");
				possibleNames.put("", "");
				break;
			case BANE_TROLL:
				possibleNames.put("Sunlight lantern", "");
				possibleNames.put("", "");
				possibleNames.put("", "");
				possibleNames.put("", "");
				possibleNames.put("", "");
				break;
			case BANE_WIZARD:
				possibleNames.put("Anti-Magic Dagger", "Cutting through spells like its nothing, this item lets you");
				possibleNames.put("Counter Spell", "");
				possibleNames.put("", "");
				possibleNames.put("", "");
				possibleNames.put("", "");
				break;
			case BANE_WOLF:
				possibleNames.put("Dog Whistle", "");
				possibleNames.put("", "");
				possibleNames.put("", "");
				possibleNames.put("", "");
				possibleNames.put("", "");
				break;
			case STATIC_AGILITY:
				possibleNames.put("", "");
				possibleNames.put("", "");
				possibleNames.put("", "");
				possibleNames.put("", "");
				possibleNames.put("", "");
				break;
			case STATIC_KNOWLEDGE:
				possibleNames.put("Adventurer's Journal", "");
				possibleNames.put("Farmer's Almanac", "");
				possibleNames.put("", "");
				possibleNames.put("", "");
				possibleNames.put("", "");
				break;
			case STATIC_MAGIC:
				possibleNames.put("Elder Oak Wand", "A wand whose core is made out of Elder oak.");
				possibleNames.put("", "");
				possibleNames.put("", "");
				possibleNames.put("", "");
				possibleNames.put("", "");
				break;
			case STATIC_MAX_ACTIVITIES:
				possibleNames.put("Jason's cup of Joe", "");
				possibleNames.put("Time Turner", "");
				possibleNames.put("Time in a bottle", "");
				possibleNames.put("", "");
				possibleNames.put("", "");
				possibleNames.put("", "");
				break;
			case STATIC_STAMINA:
				possibleNames.put("", "");
				possibleNames.put("", "");
				possibleNames.put("", "");
				possibleNames.put("", "");
				possibleNames.put("", "");
				break;
			case STATIC_STRENGTH:
				possibleNames.put("Mead Protein Shake", "");
				possibleNames.put("Cast Iron Gauntlets", "");
				possibleNames.put("", "");
				possibleNames.put("", "");
				possibleNames.put("", "");
				break;
			}
			return possibleNames;
		}
	}
	
	public enum Rarity implements Serializable {
		COMMON(1, "Common"),
		UNCOMMON(2, "Uncommon"),
		RARE(3, "Rare"),
		EPIC(4, "Epic"),
		LEGENDARY(5, "Legendary"),
		MYTHIC(6, "Mythic");
		
		private int multiplier;
		private String rarityName;
		
		Rarity(int multiplier, String rarityName) {
			this.multiplier = multiplier;
			this.rarityName = rarityName;
		}
		
		public String rarityName() {
			return rarityName;
		}

		public int getMultiplier() {
			return multiplier;
		}

		public static Rarity getRandomRarity() {
			return MYTHIC;
//			int choice = new Random().nextInt(100);
//			if(choice <= 50) {
//				return COMMON;
//			} else if (choice <= 75) {
//				return UNCOMMON;
//			} else if (choice <= 88) {
//				return RARE;
//			} else if (choice <= 94) {
//				return EPIC;
//			} else if (choice <= 98) {
//				return LEGENDARY;
//			} else {
//				return MYTHIC;
//			}
		}
	}
	
	private Rarity rarity;
	private StatType itemType;
	private String itemName, itemDescription;
	private int statIncrease;
	
	/**
	 * 
	 * @return True if item is static (stat increase)
	 */
	public boolean isStatic() {
		switch (itemType){
		case ACTIVE_AGILITY:
		case ACTIVE_GOLD:
		case ACTIVE_KNOWLEDGE:
		case ACTIVE_MAGIC:
		case ACTIVE_STAMINA:
		case ACTIVE_STRENGTH:
		case BANE_BANDIT:
		case BANE_BLOB:
		case BANE_GHOUL:
		case BANE_GIANT:
		case BANE_SKELETON:
		case BANE_TROLL:
		case BANE_WIZARD:
		case BANE_WOLF:
			return false;
		case STATIC_AGILITY:
		case STATIC_KNOWLEDGE:
		case STATIC_MAGIC:
		case STATIC_MAX_ACTIVITIES:
		case STATIC_STAMINA:
		case STATIC_STRENGTH:
			return true;
		default:
			return false;
		}
	}
	
	/**
	 * 
	 * @return True if item is active (gains more stats when a stat increases)
	 */
	public boolean isActive() {
		switch (itemType){
		case ACTIVE_AGILITY:
		case ACTIVE_GOLD:
		case ACTIVE_KNOWLEDGE:
		case ACTIVE_MAGIC:
		case ACTIVE_STAMINA:
		case ACTIVE_STRENGTH:
			return true;
		case BANE_BANDIT:
		case BANE_BLOB:
		case BANE_GHOUL:
		case BANE_GIANT:
		case BANE_SKELETON:
		case BANE_TROLL:
		case BANE_WIZARD:
		case BANE_WOLF:
		case STATIC_AGILITY:
		case STATIC_KNOWLEDGE:
		case STATIC_MAGIC:
		case STATIC_MAX_ACTIVITIES:
		case STATIC_STAMINA:
		case STATIC_STRENGTH:
			return false;
		default:
			return false;
		}
	}
	
	/**
	 * 
	 * @return True if item is a bane
	 */
	public boolean isBane() {
		switch (itemType){
		case ACTIVE_AGILITY:
		case ACTIVE_GOLD:
		case ACTIVE_KNOWLEDGE:
		case ACTIVE_MAGIC:
		case ACTIVE_STAMINA:
		case ACTIVE_STRENGTH:
		case STATIC_AGILITY:
		case STATIC_KNOWLEDGE:
		case STATIC_MAGIC:
		case STATIC_MAX_ACTIVITIES:
		case STATIC_STAMINA:
		case STATIC_STRENGTH:
			return false;
		case BANE_BANDIT:
		case BANE_BLOB:
		case BANE_GHOUL:
		case BANE_GIANT:
		case BANE_SKELETON:
		case BANE_TROLL:
		case BANE_WIZARD:
		case BANE_WOLF:
			return true;
		default:
			return false;
		}
	}

}
