package bot.role.data.structures;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.HashMap;

@Getter
@NoArgsConstructor
@ToString
public class StatBlock {
    private int magic, knowledge, stamina, strength, agility;

    /**
     *
     * @param magic
     * @param knowledge
     * @param stamina
     * @param strength
     * @param agility
     */
    public StatBlock(int magic, int knowledge, int stamina, int strength, int agility) {
        this.magic = magic;
        this.knowledge = knowledge;
        this.stamina = stamina;
        this.strength = strength;
        this.agility = agility;
    }

    @JsonIgnore
    public HashMap<String, Integer> getAllStats(){
        HashMap<String, Integer> stats = new HashMap<>();
        stats.put("Magic", magic);
        stats.put("Knowledge", knowledge);
        stats.put("Stamina", stamina);
        stats.put("Strength", strength);
        stats.put("Agility", agility);
        return stats;
    }

    public String toString(){
        String returnThis = "";
        returnThis += magic > 0 ? "Magic increased by " + magic + ", " : "";
        returnThis += knowledge > 0 ? "Knowledge increased by " + knowledge + ", " : "";
        returnThis += stamina > 0 ? "Stamina increased by " + stamina + ", " : "";
        returnThis += strength > 0 ? "Strength increased by " + strength + ", " : "";
        returnThis += agility > 0 ? "Agility increased by " + agility + ", " : "";
        return returnThis.substring(0, returnThis.length() - 2);
    }

    public int total(){
        return stamina + strength + magic + agility + knowledge;
    }

    public void addToAll(int amount){
        strength += amount;
        stamina += amount;
        magic += amount;
        agility += amount;
        knowledge += amount;
    }

    public static StatBlock add(StatBlock sb1, StatBlock sb2){
        return new StatBlock(sb1.getMagic() + sb2.getMagic(),
                sb1.getKnowledge() + sb2.getKnowledge(),
                sb1.getStamina() + sb2.getStamina(),
                sb1.getStrength() + sb2.getStrength(),
                sb1.getAgility() + sb2.getAgility());
    }

    public static String getBiggestDifference(StatBlock sb1, StatBlock sb2){
        String stat = (String)sb1.getAllStats().keySet().toArray()[0];
        int largestDif = 0;
        for(String keyStat : sb1.getAllStats().keySet()){
            int dif = sb2.getAllStats().get(keyStat) - sb1.getAllStats().get(keyStat);
            if (dif > largestDif) {
                largestDif = dif;
                stat = keyStat;
            }
        }
        return stat;
    }

    public static StatBlock generateByStat(String stat, int amount){
        StatBlock sb = new StatBlock();
        switch (stat){
            case "Magic":
                sb = new StatBlock(amount, 0,0,0,0);
                break;
            case "Knowledge":
                sb = new StatBlock(0, amount,0 ,0 ,0);
                break;
            case "Stamina":
                sb = new StatBlock(0, 0, amount, 0, 0);
                break;
            case "Strength":
                sb = new StatBlock(0, 0, 0, amount, 0);
                break;
            case "Agility":
                sb = new StatBlock(0, 0, 0, 0, amount);
                break;
        }
        return sb;
    }

}
