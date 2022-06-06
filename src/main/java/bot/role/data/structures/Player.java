package bot.role.data.structures;

import bot.role.data.item.Item;
import bot.role.data.item.Modifier;
import bot.role.data.jsonConfig.GameConfigValues;
import com.fasterxml.jackson.annotation.JsonIgnore;
import data.serializing.DataCacher;
import data.serializing.SavableData;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

@Getter
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class Player extends SavableData {

    private int strength, agility, knowledge, magic, stamina;
    private String name;
    private long gold;
    private int tournamentVictories;
    private int wins, losses;
    private int activitiesDone;
    private int challengesDefendedToday;
    private List<Item> inventory;
    private int daysSinceLastActive;
    private boolean active;

    /**
     * Creates a new player. This player gets a random stat start, and a random amount of gold
     * @param id Discord ID of player
     * @param name Discord name of player
     */
    public Player(long id, String name, GameConfigValues gcv) {
        super(id);
        Random random = new Random();
        this.name = name;
        strength = random.nextInt(gcv.getStartStatMax() + 1);
        agility = random.nextInt(gcv.getStartStatMax() + 1);
        knowledge = random.nextInt(gcv.getStartStatMax() + 1);
        magic = random.nextInt(gcv.getStartStatMax() + 1);
        stamina = random.nextInt(gcv.getStartStatMax() + 1);
        gold = random.nextInt(gcv.getStartGoldMax() + 1);
        tournamentVictories = 0;
        wins = 0;
        losses = 0;
        activitiesDone = 0;
        inventory = new LinkedList<>();
        daysSinceLastActive = 0;
        challengesDefendedToday = 0;
        active = false;
    }

    /**
     * Adds an item to the players inventory.
     * If there are more than 5 items, removes the last item in the back of the list.
     * Adds new item to the back of the list.
     * @param item Item to be added to the inventory
     */
    public void addItem(Item item){
        if(inventory.size() >= 5){
            inventory.remove(4);
        }
        inventory.add(item);
    }

    /**
     * Gets the number of activities left for the day for the player including items.
     * @return number of activities the player has left for the day
     */
    public int activitiesLeftToday(){
        int activitiesPerDay = new DataCacher<GameConfigValues>("arena\\game config data").loadSerialized().getActivitiesPerDay();
        int itemBoostActivities = getStatTotalFromItems(Modifier.Stat.ACTIVITY, Modifier.Type.STATIC);
        return activitiesPerDay + itemBoostActivities - activitiesDone;
    }

    public int defendsLeftToday(){
        int defendsPerDay = new DataCacher<GameConfigValues>("arena\\game config data").loadSerialized().getChallengeDefendPerDay();
        return defendsPerDay - challengesDefendedToday;
    }

    /**
     * Completes an amount of activities for the player
     * @param amount number of activities done
     */
    public void activityCompleted(int amount){
        activitiesDone += amount;
    }

    /**
     * Completes 1 activity for the player
     */
    public void activityCompleted(){
        activityCompleted(1);
    }

    public void updatePlayerName(String name){
        this.name = name;
    }

    /**
     * Increases the strength stat by an amount + whatever active item modifiers the player has
     * @param amount how much to increase the stat by
     */
    public void increaseStrength(int amount){
        strength += amount + getStatTotalFromItems(Modifier.Stat.STRENGTH, Modifier.Type.ACTIVE);
    }

    /**
     * Increases the stamina stat by an amount + whatever active item modifiers the player has
     * @param amount how much to increase the stat by
     */
    public void increaseStamina(int amount){
        stamina += amount + getStatTotalFromItems(Modifier.Stat.STAMINA, Modifier.Type.ACTIVE);
    }

    /**
     * Increases the agility stat by an amount + whatever active item modifiers the player has
     * @param amount how much to increase the stat by
     */
    public void increaseAgility(int amount){
        agility += amount + getStatTotalFromItems(Modifier.Stat.AGILITY, Modifier.Type.ACTIVE);
    }

    /**
     * Increases the knowledge stat by an amount + whatever active item modifiers the player has
     * @param amount how much to increase the stat by
     */
    public void increaseKnowledge(int amount){
        knowledge += amount + getStatTotalFromItems(Modifier.Stat.KNOWLEDGE, Modifier.Type.ACTIVE);
    }

    /**
     * Increases the magic stat by an amount + whatever active item modifiers the player has
     * @param amount how much to increase the stat by
     */
    public void increaseMagic(int amount){
        magic += amount + getStatTotalFromItems(Modifier.Stat.MAGIC, Modifier.Type.ACTIVE);
    }

    /**
     * Increases the gold stat by an amount + whatever active item modifiers the player has
     * @param amount how much to increase the stat by
     */
    public void increaseGold(int amount){
        gold += amount + getStatTotalFromItems(Modifier.Stat.GOLD, Modifier.Type.ACTIVE);
    }

    /**
     * Increase gold without adding in item modifiers
     * @param amount how much to crease stat by
     */
    public void increaseRawGold(int amount){
        gold += amount;
    }

    /**
     * @return Strength stat with item modifiers
     */
    @JsonIgnore
    public int getStrengthStat(){
        return strength + getStatTotalFromItems(Modifier.Stat.STRENGTH, Modifier.Type.STATIC);
    }

    /**
     * @return Magic stat with item modifiers
     */
    @JsonIgnore
    public int getMagicStat(){
        return magic + getStatTotalFromItems(Modifier.Stat.MAGIC, Modifier.Type.STATIC);
    }

    /**
     * @return Knowledge stat with item modifiers
     */
    @JsonIgnore
    public int getKnowledgeStat(){
        return knowledge + getStatTotalFromItems(Modifier.Stat.KNOWLEDGE, Modifier.Type.STATIC);
    }

    /**
     * @return Stamina stat with item modifiers
     */
    @JsonIgnore
    public int getStaminaStat(){
        return stamina + getStatTotalFromItems(Modifier.Stat.STAMINA, Modifier.Type.STATIC);
    }

    /**
     * @return Agility stat with item modifiers
     */
    @JsonIgnore
    public int getAgilityStat(){
        return agility + getStatTotalFromItems(Modifier.Stat.AGILITY, Modifier.Type.STATIC);
    }

    /**
     * @return Strength stat without item modifiers
     */
    @JsonIgnore
    public int getRawStrengthStat(){
        return strength;
    }

    /**
     * @return Magic stat without item modifiers
     */
    @JsonIgnore
    public int getRawMagicStat(){
        return magic;
    }

    /**
     * @return Knowledge stat without item modifiers
     */
    @JsonIgnore
    public int getRawKnowledgeStat(){
        return knowledge;
    }

    /**
     * @return Stamina stat without item modifiers
     */
    @JsonIgnore
    public int getRawStaminaStat(){
        return stamina;
    }

    /**
     * @return Agility stat without item modifiers
     */
    @JsonIgnore
    public int getRawAgilityStat(){
        return agility;
    }

    @JsonIgnore
    public String getStrengthToStringWithItem(){
        int itemMod = getStatTotalFromItems(Modifier.Stat.STRENGTH, Modifier.Type.STATIC);
        return strength + (itemMod > 0 ? " (+" + itemMod + ")" : "");
    }

    @JsonIgnore
    public String getStaminaToStringWithItem(){
        int itemMod = getStatTotalFromItems(Modifier.Stat.STAMINA, Modifier.Type.STATIC);
        return stamina + (itemMod > 0 ? " (+" + itemMod + ")" : "");
    }

    @JsonIgnore
    public String getKnowledgeToStringWithItem(){
        int itemMod = getStatTotalFromItems(Modifier.Stat.KNOWLEDGE, Modifier.Type.STATIC);
        return knowledge + (itemMod > 0 ? " (+" + itemMod + ")" : "");
    }

    @JsonIgnore
    public String getMagicToStringWithItem(){
        int itemMod = getStatTotalFromItems(Modifier.Stat.MAGIC, Modifier.Type.STATIC);
        return magic + (itemMod > 0 ? " (+" + itemMod + ")" : "");
    }

    @JsonIgnore
    public String getAgilityToStringWithItem(){
        int itemMod = getStatTotalFromItems(Modifier.Stat.AGILITY, Modifier.Type.STATIC);
        return agility + (itemMod > 0 ? " (+" + itemMod + ")" : "");
    }


    /**
     *
     * @param stat Stat to check items for
     * @param type Type for the item
     * @return total modifier for that stat
     */
    private int getStatTotalFromItems(Modifier.Stat stat, Modifier.Type type){
        int total = 0;
        for(int i = 0; i < 3 && i < inventory.size(); i++){
            Item item = inventory.get(i);
            if(!item.broken()){
                for(Modifier modifier : item.getModifiers()){
                    if(modifier.getStat() == stat && modifier.getType() == type){
                        total += modifier.getAmount();
                    }
                }
            }
        }
        return total;
    }

    /**
     * Sets the user to active for the day
     */
    public void setActive(){
        active = true;
    }

    /**
     * Does a new day for the user
     */
    public void newDay(){
        // stuff involving activeness
        if (active) {
            active = false;
            daysSinceLastActive = 0;
        } else {
            daysSinceLastActive++;
        }

        // stuff involving items
        for(int i = 0; i < 3 && i < inventory.size(); i++){
            Item item = inventory.get(i);
            item.useItem();
        }
    }

    /**
     * @return Stat block with modified stats due to items
     */
    @JsonIgnore
    public StatBlock getStatBlockWithItems(){
        return new StatBlock(getMagicStat(), getKnowledgeStat(), getStaminaStat(), getStrengthStat(), getAgilityStat());
    }

    /**
     * @return Stat block with raw stats
     */
    @JsonIgnore
    public StatBlock getStatBlock(){
        return new StatBlock(magic, knowledge, stamina, strength, agility);
    }

    /**
     * Taxes the player an amount of gold. Only returns the amount of gold the player is able to pay
     * @param amount Amount of gold to tax the player
     * @return Amount of gold the player could pay
     */
    public int taxGold(int amount){
        if(gold >= amount){ // has the amount of gold
            gold -= amount;
            return amount;
        }
        int total = (int)gold; // does not have the amount of gold
        gold -= gold;
        return total;
    }

}
