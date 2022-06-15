package bot.role.data.jsonConfig;


import bot.role.data.item.Modifier;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import data.serializing.SavableData;
import lombok.Getter;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

@Getter
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class Strings extends SavableData {

    private List<String[]> activeGold, activeAgility, activeKnowledge, activeMagic,
            activeStamina, activeStrength;

    private List<String[]> baneBandit, baneBlob, baneGhoul, baneGiant,
            baneSkeleton, baneTroll, baneWizard, baneWolf;

    private List<String[]> staticActivity, staticAgility, staticKnowledge, staticMagic,
            staticStamina, staticStrength;

    private List<String> dayMessageStart, dayMessageEnd;

    private List<String> agilityVendorNames, knowledgeVendorNames, magicVendorNames,
            staminaVendorNames, strengthVendorNames, goldVendorJobs;

    public Strings() {
        super("strings");
        activeAgility = new LinkedList<>();
        activeKnowledge = new LinkedList<>();
        activeMagic = new LinkedList<>();
        activeStamina = new LinkedList<>();
        activeStrength = new LinkedList<>();
        activeGold = new LinkedList<>();

        baneBandit = new LinkedList<>();
        baneBlob = new LinkedList<>();
        baneGhoul  = new LinkedList<>();
        baneGiant = new LinkedList<>();
        baneSkeleton = new LinkedList<>();
        baneTroll = new LinkedList<>();
        baneWizard = new LinkedList<>();
        baneWolf = new LinkedList<>();

        staticAgility = new LinkedList<>();
        staticKnowledge = new LinkedList<>();
        staticMagic = new LinkedList<>();
        staticStamina = new LinkedList<>();
        staticStrength = new LinkedList<>();
        staticActivity = new LinkedList<>();

        dayMessageStart = new LinkedList<>();
        dayMessageEnd = new LinkedList<>();

        agilityVendorNames = new LinkedList<>();
        knowledgeVendorNames = new LinkedList<>();
        magicVendorNames = new LinkedList<>();
        staminaVendorNames = new LinkedList<>();
        strengthVendorNames = new LinkedList<>();
        goldVendorJobs = new LinkedList<>();

        agilityVendorNames.add("Tinúviel");
        agilityVendorNames.add("Knife Master Uragu");
        agilityVendorNames.add("A Nimble Thief");
        agilityVendorNames.add("Robin the hooded");
        agilityVendorNames.add("Vestman Victor");
        agilityVendorNames.add("A Highwayman Voronin");

        knowledgeVendorNames.add("A Renown Scholar");
        knowledgeVendorNames.add("A Penniless Educator");
        knowledgeVendorNames.add("A Curious Librarian");
        knowledgeVendorNames.add("Bee farmer Herrold");
        knowledgeVendorNames.add("Professor Rendtyk");

        magicVendorNames.add("A Mystical Conjuror");
        magicVendorNames.add("A Luthwin Caster");
        magicVendorNames.add("Zack the Mage");
        magicVendorNames.add("Azathoth the cursed");
        magicVendorNames.add("A Hooded Figure");
        magicVendorNames.add("Ymir the fallen");

        staminaVendorNames.add("Boic the Brave");
        staminaVendorNames.add("A Seasoned Warrior");
        staminaVendorNames.add("A dark Assassin");
        staminaVendorNames.add("Charlie Lehorse");
        staminaVendorNames.add("Sword Master Sketh");

        strengthVendorNames.add("A Goliath");
        strengthVendorNames.add("Sir Kendrith the 4th");
        strengthVendorNames.add("Garrison Commander Lukasz");
        strengthVendorNames.add("Strongarm Zuq");
        strengthVendorNames.add("Nagrog the beast");

        goldVendorJobs.add("Clean the stables");
        goldVendorJobs.add("Scrub the church stones");
        goldVendorJobs.add("Stone the criminals");
        goldVendorJobs.add("Sweep the castle");
        goldVendorJobs.add("Dump the chamberpots");
        goldVendorJobs.add("Clean the bloody swords");
        goldVendorJobs.add("Educate the children");
        goldVendorJobs.add("Hold the Shlongbot trials");
        goldVendorJobs.add("Break up the worker strikes");
        goldVendorJobs.add("Harvest the beehives");
        goldVendorJobs.add("Breastfeed the babies");
        goldVendorJobs.add("Sweeten the honey");
        goldVendorJobs.add("Pray at the shrine for shlongbot");
        goldVendorJobs.add("Revive the peasants");
        goldVendorJobs.add("Fight Kat's principal Jeff");

        dayMessageStart.add("The sun rises on our wonderful kingdom once again.");
        dayMessageStart.add("The light shines through the stained glass portrait of myself, the king/queen.");
        dayMessageStart.add("Like honey slowly drooping, light bathes the kingdom in its golden glow.");
        dayMessageStart.add("The bees buzz with content as a night of rest comes to an end.");
        dayMessageStart.add("New day, new prospects.");
        dayMessageStart.add("Like an angry hive, the denizens buzz excitedly for the start of a new day.");
        dayMessageStart.add("Good morning my children!");
        dayMessageStart.add("The glint from my crown awakens me.");
        dayMessageStart.add("Light begins to pour into the streets below.");
        dayMessageStart.add("Chiming sounds throughout the castle halls as the sun rises above the horizon.");
        dayMessageStart.add("A rooster bores its noise into the ears of all who were in the village, abruptly seizing as a morningï¿½s hunger sets in.");

        dayMessageEnd.add("I can already hear swords being drawn");
        dayMessageEnd.add("I wonder what threats we shall see today");
        dayMessageEnd.add("I can hear the birds chirping, the cows mooing, and my children already beating the crap out of each other");
        dayMessageEnd.add("Is the red color in the streets new?");
        dayMessageEnd.add("Another day, another bloodbath");
        dayMessageEnd.add("*Screams of agony* .....Wonderful");
        dayMessageEnd.add("Squire, does any of this really matter? Like really *really* matter?");
        dayMessageEnd.add("Is shlongbot opposed to all the violence or does it like to watch it like some festivities?");
        dayMessageEnd.add("The church of shlongbot should be crowded today. A lot of questionable things happened yesterday...");
        dayMessageEnd.add("Will the monarchy be overthrown today? Or perhaps the communist revolution? I can hardly wait to find out");
        dayMessageEnd.add("Nothing like the scent of fresh stats in the morning");
        dayMessageEnd.add("Do keep it down today children, we do not want to upset the bees");
        dayMessageEnd.add("Revealed are hordes of undefeated monsters. Perhaps you should get on that?");

        activeAgility.add(toStringArray("Adventurer's Belt", "The leather speaks to you as you fight."));
        activeAgility.add(toStringArray("Woolly Scarf", "The patterns make me feel warm."));
        activeAgility.add(toStringArray("Durable Gloves", "Double stitched seams, padded palms, made for the working person."));
        activeAgility.add(toStringArray("Ring of Agility", "This ring is imbued with magic, making you feel more agile."));
        activeAgility.add(toStringArray("Comfy Socks", "A comfy fighter is an agile one."));

        activeKnowledge.add(toStringArray("Scroll of Java", "Ancient script that gives the knowledge of manipulation."));
        activeKnowledge.add(toStringArray("All seeing Spectacles", "Comfortable, and fashionable."));
        activeKnowledge.add(toStringArray("Rob's Tome", "The entire coagulation of Rob’s knowledge flows through you."));
        activeKnowledge.add(toStringArray("Euth's Magnifying Glass", "A glass lens that lets you see things in a different light."));
        activeKnowledge.add(toStringArray("Scribe's Feather Pen", "Scholars of the ages used this pen. Their knowledge kept in the everlasting ink."));

        activeMagic.add(toStringArray("Crystal Ball", "The secrets of arcane can be seen through this ball."));
        activeMagic.add(toStringArray("Enchanted Necklace", "The magic in this necklace increases your power."));
        activeMagic.add(toStringArray("Ingredients Bag", "Always having a place for your things lets you access them more quickly"));
        activeMagic.add(toStringArray("Charged Gemstone", "A gemstone filled with mana lets you tap into its reserves when you most need it."));
        activeMagic.add(toStringArray("Dragon's Bone Wand", "The knowledge of the arcane flows from the wand into you, great giant beasts bequeath you their knowledge."));

        activeStamina.add(toStringArray("Rejuvenating Potion", "Your muscles grow strong again, ready to fight once more."));
        activeStamina.add(toStringArray("Rope of Jumping", "Cardio is very important."));
        activeStamina.add(toStringArray("Weighted Anklets", "The Extra weight lets you train faster."));
        activeStamina.add(toStringArray("Ring of Ragivee", "This ring seems a little small?"));
        activeStamina.add(toStringArray("Training Regimen", "A well rounded workout routine."));

        activeStrength.add(toStringArray("Weighted Ropes", "Regular ropes aren’t enough anymore."));
        activeStrength.add(toStringArray("Heavy Sword", "A sword that weights more is harder to stop."));
        activeStrength.add(toStringArray("Mammoth Essence", "The strength of this great beast flows through you!"));
        activeStrength.add(toStringArray("Powder of Whey", "This magic powder increases the strength of your muscles."));
        activeStrength.add(toStringArray("Pig Skin Sack", "For punching"));

        activeGold.add(toStringArray("Coin Purse", "You always seem to find more gold you forgot about in here."));
        activeGold.add(toStringArray("Rabbit's Foot", "Lucky rabbits foot lets you find more gold."));
        activeGold.add(toStringArray("Ring of Gold", "A ring enchanted to let you find more gold."));
        activeGold.add(toStringArray("Ticket of Fortune", "A parchment infused with magic and luck."));
        activeGold.add(toStringArray("Token of Wealth", "A lucky coin that gives you more gold."));

        staticActivity.add(toStringArray("Jason's cup of Joe", "Nothing like a good cup of coffee to help your daily productivity."));
        staticActivity.add(toStringArray("Time Turner", "Time in the palm of your hands."));
        staticActivity.add(toStringArray("Time in a Bottle", "Opening this bottle brings you back in time, allowing you to do more in the day."));
        staticActivity.add(toStringArray("Scroll of Haste", "Everything around you is slower, including time itself."));
        staticActivity.add(toStringArray("Shadow Familiar", "If only there were two of you..."));

        staticAgility.add(toStringArray("Aerodynamic Garments", "Air friction has nothing on these clothes."));
        staticAgility.add(toStringArray("Winged Shoes", "Lighter shoes allow you to move quicker and more quietly."));
        staticAgility.add(toStringArray("Band Swift Skin", "Your body feels lighter and more agile."));
        staticAgility.add(toStringArray("Recurve Bow", "This fancy bow shoots arrows with ease."));
        staticAgility.add(toStringArray("Thieves gloves", "Worn by a master thief, these gloves already know how to steal. They are also fingerless."));

        staticKnowledge.add(toStringArray("Adventurer's Journal", "The notes of a seasoned adventurer are scribbled down for you to learn."));
        staticKnowledge.add(toStringArray("Farmer's Almanac", "An Almanac filled with knowledge on crops, food, and farming."));
        staticKnowledge.add(toStringArray("Hive's Mind", "The mind of the hive, a strange buzzing sound somehow translates to coherent thoughts."));
        staticKnowledge.add(toStringArray("Study Tome", "A tome of knowledge."));
        staticKnowledge.add(toStringArray("Vestment of Intelligence", "You feel smarter wearing this garment."));

        staticMagic.add(toStringArray("Elder Oak Wand", "A wand whose core is made out of Elder oak."));
        staticMagic.add(toStringArray("Animated Golem", "A small magical golem that aids you in battle. It is smooth and coppery."));
        staticMagic.add(toStringArray("Mana Lens", "Looking through this lens grants you the ability to see how magic flows."));
        staticMagic.add(toStringArray("Dark Ritual", "This ritual fills your body with dark magic."));
        staticMagic.add(toStringArray("Decree of Magic", "This is a paper with a blank line on it for your name. It says that you know magic."));

        staticStamina.add(toStringArray("Jason's cup of Joe", "Nothing like a good cup of coffee to help your daily productivity."));
        staticStamina.add(toStringArray("Time Turner", "Time in the palm of your hands."));
        staticStamina.add(toStringArray("Time in a Bottle", "Opening this bottle brings you back in time, allowing you to do more in the day."));
        staticStamina.add(toStringArray("Scroll of Haste", "Everything around you is slower, including time itself."));
        staticStamina.add(toStringArray("Shadow Familiar", "If only there were two of you..."));

        staticStrength.add(toStringArray("Mead Protein Shake", "What is that flavor? ...is that grape?"));
        staticStrength.add(toStringArray("Iron Gauntlets", "Don’t forget the padding underneath the chainmail."));
        staticStrength.add(toStringArray("Rusty Dumbbells", "For punching"));
        staticStrength.add(toStringArray("Armor of the Mountain King", "The great Mountain King’s armor, the historic fights this armor has seen."));
        staticStrength.add(toStringArray("Tower Shield of Cowardice", "You can't fight what you can't see."));

        baneBandit.add(toStringArray("Net Trap", "Even the most clever of bandits won’t see this coming"));
        baneBandit.add(toStringArray("Farmers Cloak", "Gives the wearer the appearance of a farmer. Not too appetizing for a mugging"));
        baneBandit.add(toStringArray("Tracking Arrow", "An arrow that never misses its target... except its only targets are Bandits."));

        baneBlob.add(toStringArray("Anticoagulant Bucket", "Is that semi-solid liquid I see approaching? Nothing a little anticoagulant can’t handle."));
        baneBlob.add(toStringArray("Fire Bomb", "You cannot blob me if you are evaporated."));
        baneBlob.add(toStringArray("Denaturization Falchion", "What kind of amalgamation are you if you have no protein bonds holding you together?"));

        baneGhoul.add(toStringArray("Flash Bomb", "The power of the sun in the palm of your hand."));
        baneGhoul.add(toStringArray("Tears of Shlongbot", "Holy water from the eyes of Shlongbot itself."));
        baneGhoul.add(toStringArray("Sun Charged Blade", "A blade imbued with the light of day."));

        baneGiant.add(toStringArray("Trip Wire", "One string is all you need."));
        baneGiant.add(toStringArray("Viper's Poison Dart", "A powerful poison to take down your powerful foe."));
        baneGiant.add(toStringArray("Mammoth Tusk Spear", "A strong spear made from Mammoth tusk."));

        baneSkeleton.add(toStringArray("Bone Breaker", "This hammer smashes through the brittle bones of skeletons."));
        baneSkeleton.add(toStringArray("Acid Splash Potion", "Melting the bones of your enemy is one way to deal with them."));
        baneSkeleton.add(toStringArray("Osteotome", "A tome with knowledge on how to brittle the bones of your foe."));

        baneTroll.add(toStringArray("Sunlight lantern", "Turn the troll to stone with this handy lantern."));
        baneTroll.add(toStringArray("Swift Glaive", "A Glaive faster than anything this troll has seen."));
        baneTroll.add(toStringArray("Scroll of Fireball", "Massive explosions can’t be good for their skin."));

        baneWizard.add(toStringArray("Anti-Magic Dagger", "Cutting through spells like its nothing, this item lets you"));
        baneWizard.add(toStringArray("Counter Spell", "The Wizard’s greatest weakness, no magic."));
        baneWizard.add(toStringArray("Null Shield", "Nullify any spell with this shield."));

        baneWolf.add(toStringArray("Wolf Whistle", "A whistle that makes the wolf go away leaving its loot behind."));
        baneWolf.add(toStringArray("Beast Talisman", "A talisman enchanted to help against beasts."));
        baneWolf.add(toStringArray("Fluffy Collar", "Pink, stylish, and it has spikes."));
    }

    private String[] toStringArray(String...strings){
        return strings.clone();
    }

    public String[] getItemName(Modifier mod){
        switch (mod.getType()){
            case STATIC:
                switch (mod.getStat()){
                    case STRENGTH:
                        return getStaticStrengthItem();
                    case MAGIC:
                        return getStaticMagicItem();
                    case KNOWLEDGE:
                        return getStaticKnowledgeItem();
                    case AGILITY:
                        return getStaticAgilityItem();
                    case STAMINA:
                        return getStaticStaminaItem();
                    case ACTIVITY:
                        return getStaticActivityItem();
                }
                break;
            case ACTIVE:
                switch(mod.getStat()){
                    case STRENGTH:
                        return getActiveStrengthItem();
                    case MAGIC:
                        return getActiveMagicItem();
                    case KNOWLEDGE:
                        return getActiveKnowledgeItem();
                    case AGILITY:
                        return getActiveAgilityItem();
                    case STAMINA:
                        return getActiveStaminaItem();
                    case GOLD:
                        return getActiveGoldItem();
                }
                break;
            case BANE:
                switch (mod.getStat()){
                    case BLOB:
                        return getBaneBlobItem();
                    case WIZARD:
                        return getBaneWizardItem();
                    case WOLF:
                        return getBaneWolfItem();
                    case BANDIT:
                        return getBaneBanditItem();
                    case GIANT:
                        return getBaneGiantItem();
                    case TROLL:
                        return getBaneTrollItem();
                    case GHOUL:
                        return getBaneGoulItem();
                    case SKELETON:
                        return getBaneSkeletonItem();
                }
                break;
        }
        return null;
    }
    /**
     * @return a random agility vendor name
     */
    @JsonIgnore
    public String getAgilityVendorName(){
        Random random = new Random();
        return agilityVendorNames.get(random.nextInt(agilityVendorNames.size()));
    }

    /**
     * @return a random knowledge vendor name
     */
    @JsonIgnore
    public String getKnowledgeVendorName(){
        Random random = new Random();
        return knowledgeVendorNames.get(random.nextInt(knowledgeVendorNames.size()));
    }

    /**
     * @return a random magic vendor name
     */
    @JsonIgnore
    public String getMagicVendorName(){
        Random random = new Random();
        return magicVendorNames.get(random.nextInt(magicVendorNames.size()));
    }

    /**
     * @return a random stamina vendor name
     */
    @JsonIgnore
    public String getStaminaVendorName(){
        Random random = new Random();
        return staminaVendorNames.get(random.nextInt(staminaVendorNames.size()));
    }

    /**
     * @return a random strength vendor name
     */
    @JsonIgnore
    public String getStrengthVendorName(){
        Random random = new Random();
        return strengthVendorNames.get(random.nextInt(strengthVendorNames.size()));
    }

    /**
     * @return a random gold job
     */
    @JsonIgnore
    public String getGoldJobName(){
        Random random = new Random();
        return goldVendorJobs.get(random.nextInt(goldVendorJobs.size()));
    }

    /**
     * @return a random string to start a new day message
     */
    @JsonIgnore
    public String getDayMessageStart(){
        Random random = new Random();
        return dayMessageStart.get(random.nextInt(dayMessageStart.size()));
    }

    /**
     * @return a random string to end a new day message
     */
    @JsonIgnore
    public String getDayMessageEnd(){
        Random random = new Random();
        return dayMessageEnd.get(random.nextInt(dayMessageEnd.size()));
    }

    /**
     * @return a random item name and description pair for static agility
     */
    @JsonIgnore
    public String[] getStaticAgilityItem(){
        Random random = new Random();
        return staticAgility.get(random.nextInt(staticAgility.size()));
    }

    /**
     * @return a random item name and description pair for static knowledge
     */
    @JsonIgnore
    public String[] getStaticKnowledgeItem(){
        Random random = new Random();
        return staticKnowledge.get(random.nextInt(staticKnowledge.size()));
    }

    /**
     * @return a random item name and description pair for static magic
     */
    @JsonIgnore
    public String[] getStaticMagicItem(){
        Random random = new Random();
        return staticMagic.get(random.nextInt(staticMagic.size()));
    }

    /**
     * @return a random item name and description pair for static stamina
     */
    @JsonIgnore
    public String[] getStaticStaminaItem(){
        Random random = new Random();
        return staticStamina.get(random.nextInt(staticStamina.size()));
    }

    /**
     * @return a random item name and description pair for static strength
     */
    @JsonIgnore
    public String[] getStaticStrengthItem(){
        Random random = new Random();
        return staticStrength.get(random.nextInt(staticStrength.size()));
    }

    /**
     * @return a random item name and description pair for static activity
     */
    @JsonIgnore
    public String[] getStaticActivityItem(){
        Random random = new Random();
        return staticActivity.get(random.nextInt(staticActivity.size()));
    }

    /**
     * @return a random item name and description pair for active agility
     */
    @JsonIgnore
    public String[] getActiveAgilityItem(){
        Random random = new Random();
        return activeAgility.get(random.nextInt(activeAgility.size()));
    }

    /**
     * @return a random item name and description pair for active knowledge
     */
    @JsonIgnore
    public String[] getActiveKnowledgeItem(){
        Random random = new Random();
        return activeKnowledge.get(random.nextInt(activeKnowledge.size()));
    }

    /**
     * @return a random item name and description pair for active magic
     */
    @JsonIgnore
    public String[] getActiveMagicItem(){
        Random random = new Random();
        return activeMagic.get(random.nextInt(activeMagic.size()));
    }

    /**
     * @return a random item name and description pair for active stamina
     */
    @JsonIgnore
    public String[] getActiveStaminaItem(){
        Random random = new Random();
        return activeStamina.get(random.nextInt(activeStamina.size()));
    }

    /**
     * @return a random item name and description pair for active strength
     */
    @JsonIgnore
    public String[] getActiveStrengthItem(){
        Random random = new Random();
        return activeStrength.get(random.nextInt(activeStrength.size()));
    }

    /**
     * @return a random item name and description pair for active gold
     */
    @JsonIgnore
    public String[] getActiveGoldItem(){
        Random random = new Random();
        return activeGold.get(random.nextInt(activeGold.size()));
    }

    /**
     * @return a random item name and description pair for bane bandit
     */
    @JsonIgnore
    public String[] getBaneBanditItem(){
        Random random = new Random();
        return baneBandit.get(random.nextInt(baneBandit.size()));
    }

    /**
     * @return a random item name and description pair for bane blob
     */
    @JsonIgnore
    public String[] getBaneBlobItem(){
        Random random = new Random();
        return baneBlob.get(random.nextInt(baneBlob.size()));
    }

    /**
     * @return a random item name and description pair for bane goul
     */
    @JsonIgnore
    public String[] getBaneGoulItem(){
        Random random = new Random();
        return baneGhoul.get(random.nextInt(baneGhoul.size()));
    }

    /**
     * @return a random item name and description pair for bane giant
     */
    @JsonIgnore
    public String[] getBaneGiantItem(){
        Random random = new Random();
        return baneGiant.get(random.nextInt(baneGiant.size()));
    }

    /**
     * @return a random item name and description pair for bane skeleton
     */
    @JsonIgnore
    public String[] getBaneSkeletonItem(){
        Random random = new Random();
        return baneSkeleton.get(random.nextInt(baneSkeleton.size()));
    }

    /**
     * @return a random item name and description pair for bane Troll
     */
    @JsonIgnore
    public String[] getBaneTrollItem(){
        Random random = new Random();
        return baneTroll.get(random.nextInt(baneTroll.size()));
    }

    /**
     * @return a random item name and description pair for bane wizard
     */
    @JsonIgnore
    public String[] getBaneWizardItem(){
        Random random = new Random();
        return baneWizard.get(random.nextInt(baneWizard.size()));
    }

    /**
     * @return a random item name and description pair for bane wolf
     */
    @JsonIgnore
    public String[] getBaneWolfItem(){
        Random random = new Random();
        return baneWolf.get(random.nextInt(baneWolf.size()));
    }
}