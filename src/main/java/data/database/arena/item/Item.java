package data.database.arena.item;

import java.util.HashMap;
import java.util.Random;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Embeddable
public class Item {

	public enum StatType {
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
			int choice = new Random().nextInt(14);
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
				possibleNames.put("Adventurer's Belt", "The leather speaks to you as you fight.");
				possibleNames.put("Woolly Scarf", "The patterns make me feel warm.");
				possibleNames.put("Durable Gloves", "Double stitched seams, padded palms, made for the working person.");
				possibleNames.put("Ring of Agility", "This ring is imbued with magic, making you feel more agile.");
				possibleNames.put("Comfy Socks", "A comfy fighter is an agile one.");
				break;
			case ACTIVE_GOLD:
				possibleNames.put("Coin Purse", "You always seem to find more gold you forgot about in here.");
				possibleNames.put("Rabbit's Foot", "Lucky rabbits foot lets you find more gold.");
				possibleNames.put("Ring of Gold", "A ring enchanted to let you find more gold.");
				possibleNames.put("Ticket of Fortune", "A parchment infused with magic and luck.");
				possibleNames.put("Token of Wealth", "A lucky coin that gives you more gold.");
				break;
			case ACTIVE_KNOWLEDGE:
				possibleNames.put("Scroll of Java", "Ancient script that gives the knowledge of manipulation.");
				possibleNames.put("All seeing Spectacles", "Comfortable, and fashionable.");
				possibleNames.put("Rob's Tome", "The entire coagulation of Rob’s knowledge flows through you.");
				possibleNames.put("Euth's Magnifying Glass", "A glass lens that lets you see things in a different light.");
				possibleNames.put("Scribe's Feather Pen", "Scholars of the ages used this pen. Their knowledge kept in the everlasting ink.");
				break;
			case ACTIVE_MAGIC:
				possibleNames.put("Crystal Ball", "The secrets of arcane can be seen through this ball.");
				possibleNames.put("Enchanted Necklace", "The magic in this necklace increases your power.");
				possibleNames.put("Ingredients Bag", "Always having a place for your things lets you access them more quickly");
				possibleNames.put("Charged Gemstone", "A gemstone filled with mana lets you tap into its reserves when you most need it.");
				possibleNames.put("Dragon's Bone Wand", "The knowledge of the arcane flows from the wand into you, great giant beasts bequeath you their knowledge.");
				break;
			case ACTIVE_STAMINA:
				possibleNames.put("Rejuvenating Potion", "Your muscles grow strong again, ready to fight once more.");
				possibleNames.put("Rope of Jumping", "Cardio is very important.");
				possibleNames.put("Weighted Anklets", "The Extra weight lets you train faster.");
				possibleNames.put("Ring of Ragivee", "This ring seems a little small?");
				possibleNames.put("Training Regimen", "A well rounded workout routine.");
				break;
			case ACTIVE_STRENGTH:
				possibleNames.put("Weighted Ropes", "Regular ropes aren’t enough anymore.");
				possibleNames.put("Heavy Sword", "A sword that weights more is harder to stop.");
				possibleNames.put("Mammoth Essence", "The strength of this great beast flows through you!");
				possibleNames.put("Powder of Whey", "This magic powder increases the strength of your muscles.");
				possibleNames.put("Pig Skin Sack", "For punching");
				break;
			case BANE_BANDIT:
				possibleNames.put("Net Trap", "Even the most clever of bandits won’t see this coming");
				possibleNames.put("Farmers Cloak", "Gives the wearer the appearance of a farmer. Not too appetizing for a mugging");
				possibleNames.put("Tracking Arrow", "An arrow that never misses its target... except its only targets are Bandits.");
				break;
			case BANE_BLOB:
				possibleNames.put("Anticoagulant Bucket", "Is that semi-solid liquid I see approaching? Nothing a little anticoagulant can’t handle.");
				possibleNames.put("Fire Bomb", "You cannot blob me if you are evaporated.");
				possibleNames.put("Denaturization Falchion", "What kind of amalgamation are you if you have no protein bonds holding you together?");
				break;
			case BANE_GHOUL:
				possibleNames.put("Flash Bomb", "The power of the sun in the palm of your hand.");
				possibleNames.put("Tears of Shlongbot", "Holy water from the eyes of Shlongbot itself.");
				possibleNames.put("Sun Charged Blade", "A blade imbued with the light of day.");
				break;
			case BANE_GIANT:
				possibleNames.put("Trip Wire", "One string is all you need.");
				possibleNames.put("Viper's Poison Dart", "A powerful poison to take down your powerful foe.");
				possibleNames.put("Mammoth Tusk Spear", "A strong spear made from Mammoth tusk.");
				break;
			case BANE_SKELETON:
				possibleNames.put("Bone Breaker", "This hammer smashes through the brittle bones of skeletons.");
				possibleNames.put("Acid Splash Potion", "Melting the bones of your enemy is one way to deal with them.");
				possibleNames.put("Osteotome", "A tome with knowledge on how to brittle the bones of your foe.");
				break;
			case BANE_TROLL:
				possibleNames.put("Sunlight lantern", "Turn the troll to stone with this handy lantern.");
				possibleNames.put("Swift Glaive", "A Glaive faster than anything this troll has seen.");
				possibleNames.put("Scroll of Fireball", "Massive explosions can’t be good for their skin.");
				break;
			case BANE_WIZARD:
				possibleNames.put("Anti-Magic Dagger", "Cutting through spells like its nothing, this item lets you");
				possibleNames.put("Counter Spell", "The Wizard’s greatest weakness, no magic.");
				possibleNames.put("Null Shield", "Nullify any spell with this shield.");
				break;
			case BANE_WOLF:
				possibleNames.put("Wolf Whistle", "A whistle that makes the wolf go away leaving its loot behind.");
				possibleNames.put("Beast Talisman", "A talisman enchanted to help against beasts.");
				possibleNames.put("Fluffy Collar", "Pink, stylish, and it has spikes.");
				break;
			case STATIC_AGILITY:
				possibleNames.put("Aerodynamic Garments", "Air friction has nothing on these clothes.");
				possibleNames.put("Winged Shoes", "Lighter shoes allow you to move quicker and more quietly.");
				possibleNames.put("Band Swift Skin", "Your body feels lighter and more agile.");
				possibleNames.put("Recurve Bow", "This fancy bow shoots arrows with ease.");
				possibleNames.put("Thieves gloves", "Worn by a master thief, these gloves already know how to steal. They are also fingerless.");
				break;
			case STATIC_KNOWLEDGE:
				possibleNames.put("Adventurer's Journal", "The notes of a seasoned adventurer are scribbled down for you to learn.");
				possibleNames.put("Farmer's Almanac", "An Almanac filled with knowledge on crops, food, and farming.");
				possibleNames.put("Hive's Mind", "The mind of the hive, a strange buzzing sound somehow translates to coherent thoughts.");
				possibleNames.put("Study Tome", "A tome of knowledge.");
				possibleNames.put("Vestment of Intelligence", "You feel smarter wearing this garment.");
				break;
			case STATIC_MAGIC:
				possibleNames.put("Elder Oak Wand", "A wand whose core is made out of Elder oak.");
				possibleNames.put("Animated Golem", "A small magical golem that aids you in battle. It is smooth and coppery.");
				possibleNames.put("Mana Lens", "Looking through this lens grants you the ability to see how magic flows.");
				possibleNames.put("Dark Ritual", "This ritual fills your body with dark magic.");
				possibleNames.put("Decree of Magic", "This is a paper with a blank line on it for your name. It says that you know magic.");
				break;
			case STATIC_MAX_ACTIVITIES:
				possibleNames.put("Jason's cup of Joe", "Nothing like a good cup of coffee to help your daily productivity.");
				possibleNames.put("Time Turner", "Time in the palm of your hands.");
				possibleNames.put("Time in a Bottle", "Opening this bottle brings you back in time, allowing you to do more in the day.");
				possibleNames.put("Scroll of Haste", "Everything around you is slower, including time itself.");
				possibleNames.put("Shadow Familiar", "If only there were two of you...");
				break;
			case STATIC_STAMINA:
				possibleNames.put("Brewed Concoction", "A local brewery came out with this one. They say it's wheat based.");
				possibleNames.put("Bison Grass Potion", "From the master alchemist himself. She says it increases your strength.");
				possibleNames.put("Smelling Salts of Vigor", "The mystic of shlongshot made this. You feel as though everything is lighter.");
				possibleNames.put("Isotonic Concoction", "This makes you feel as if your muscles are controlled by something else.");
				possibleNames.put("Potion of Steadfast", "This potion allows you to keep on fighting, even if you are tired.");
				break;
			case STATIC_STRENGTH:
				possibleNames.put("Mead Protein Shake", "What is that flavor? ...is that grape?");
				possibleNames.put("Iron Gauntlets", "Don’t forget the padding underneath the chainmail.");
				possibleNames.put("Rusty Dumbbells", "For punching");
				possibleNames.put("Armor of the Mountain King", "The great Mountain King’s armor, the historic fights this armor has seen.");
				possibleNames.put("Tower Shield of Cowardice", "You can't fight what you can't see.");
				break;
			}
			return possibleNames;
		}
	}
	
	public enum Rarity {
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
			int choice = new Random().nextInt(100);
			if(choice <= 50) {
				return COMMON;
			} else if (choice <= 75) {
				return UNCOMMON;
			} else if (choice <= 88) {
				return RARE;
			} else if (choice <= 94) {
				return EPIC;
			} else if (choice <= 98) {
				return LEGENDARY;
			} else {
				return MYTHIC;
			}
		}
	}
	@Column(nullable = true)
	private Rarity rarity;
	@Column(nullable = true)
	private StatType itemType;
	@Column(nullable = true)
	private String itemName;
	@Column(nullable = true)
	private String itemDescription;
	@Column(nullable = true)
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

	public Item(bot.role.data.Item item) {
		switch (item.getRarity()) {
		case COMMON:
			rarity = Rarity.COMMON;
			break;
		case EPIC:
			rarity = Rarity.EPIC;
			break;
		case LEGENDARY:
			rarity = Rarity.LEGENDARY;
			break;
		case MYTHIC:
			rarity = Rarity.MYTHIC;
			break;
		case RARE:
			rarity = Rarity.RARE;
			break;
		case UNCOMMON:
			rarity = Rarity.UNCOMMON;
			break;		
		}
		switch (item.getItemType()) {
		case ACTIVE_AGILITY:
			itemType = StatType.ACTIVE_AGILITY;
			break;
		case ACTIVE_GOLD:
			itemType = StatType.ACTIVE_GOLD;
			break;
		case ACTIVE_KNOWLEDGE:
			itemType = StatType.ACTIVE_KNOWLEDGE;
			break;
		case ACTIVE_MAGIC:
			itemType = StatType.ACTIVE_MAGIC;
			break;
		case ACTIVE_STAMINA:
			itemType = StatType.ACTIVE_STAMINA;
			break;
		case ACTIVE_STRENGTH:
			itemType = StatType.ACTIVE_STRENGTH;
			break;
		case BANE_BANDIT:
			itemType = StatType.BANE_BANDIT;
			break;
		case BANE_BLOB:
			itemType = StatType.BANE_BLOB;
			break;
		case BANE_GHOUL:
			itemType = StatType.BANE_GHOUL;
			break;
		case BANE_GIANT:
			itemType = StatType.BANE_GIANT;
			break;
		case BANE_SKELETON:
			itemType = StatType.BANE_SKELETON;
			break;
		case BANE_TROLL:
			itemType = StatType.BANE_TROLL;
			break;
		case BANE_WIZARD:
			itemType = StatType.BANE_WIZARD;
			break;
		case BANE_WOLF:
			itemType = StatType.BANE_WOLF;
			break;
		case STATIC_AGILITY:
			itemType = StatType.STATIC_AGILITY;
			break;
		case STATIC_KNOWLEDGE:
			itemType = StatType.STATIC_KNOWLEDGE;
			break;
		case STATIC_MAGIC:
			itemType = StatType.STATIC_MAGIC;
			break;
		case STATIC_MAX_ACTIVITIES:
			itemType = StatType.STATIC_MAX_ACTIVITIES;
			break;
		case STATIC_STAMINA:
			itemType = StatType.STATIC_STAMINA;
			break;
		case STATIC_STRENGTH:
			itemType = StatType.STATIC_STRENGTH;
			break;		
		}
		itemName = item.getItemName();
		itemDescription = item.getItemDescription();
		statIncrease = item.getStatIncrease();
	}

}
