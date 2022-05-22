package bot.role.data;

import bot.role.data.item.Item;
import bot.role.data.item.Modifier;
import bot.role.data.jsonConfig.GameConfigValues;
import com.fasterxml.jackson.annotation.JsonIgnore;
import data.serializing.DataCacher;
import data.serializing.SaveableData;
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
public class Player extends SaveableData {

    private int strength, agility, knowledge, magic, stamina;
    private String name;
    private String rank;
    private long gold;
    private int tournamentVictories;
    private int wins, losses;
    private int activitiesDone;
    private List<Item> inventory;
    private int daysSinceLastActive;

    /**
     * Creates a new player. This player gets a random stat start, and a random amount of gold
     * @param id Discord ID of player
     * @param name Discord name of player
     */
    public Player(long id, String name) {
        super(id);
        GameConfigValues gcv = new DataCacher<GameConfigValues>("game_config").loadSerialized();
        Random random = new Random();
        this.name = name;
        rank = "";
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
        int activitiesPerDay = new DataCacher<GameConfigValues>("game_config").loadSerialized().getActivitiesPerDay();
        int itemBoostActivities = getStatTotalFromItems(Modifier.Stat.ACTIVITY, Modifier.Type.STATIC);
        return activitiesPerDay + itemBoostActivities - activitiesDone;
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

    /**
     *
     * @param stat Stat to check items for
     * @return total modifier for that stat
     */
    private int getStatTotalFromItems(Modifier.Stat stat, Modifier.Type type){
        int total = 0;
        for(int i = 0; i < 3 && i < inventory.size(); i++){
            Item item = inventory.get(i);
            for(Modifier modifier : item.getModifiers()){
                if(modifier.getStat() == stat && modifier.getType() == type){
                    total += modifier.getAmount();
                }
            }
        }
        return total;
    }

}
