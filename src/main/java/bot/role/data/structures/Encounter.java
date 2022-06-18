package bot.role.data.structures;

import bot.role.data.structures.item.Modifier;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import data.serializing.SavableData;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.Random;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class Encounter extends SavableData {
    private int magic, knowledge, strength, stamina, agility;
    private int goldReward;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date departs;
    private String name;
    private Modifier.Stat baneType;

    /**
     *
     * @param id
     * @param magic
     * @param knowledge
     * @param strength
     * @param stamina
     * @param agility
     * @param goldReward
     * @param name
     */
    public Encounter(long id, int magic, int knowledge, int strength, int stamina, int agility, int goldReward, String name, Date departs) {
        super(id);
        this.magic = magic;
        this.knowledge = knowledge;
        this.strength = strength;
        this.stamina = stamina;
        this.agility = agility;
        this.goldReward = goldReward;
        this.name = name;
        this.departs = departs;
    }

    public Encounter(int magic, int knowledge, int strength, int stamina, int agility, int goldReward) {
        this.magic = magic;
        this.knowledge = knowledge;
        this.strength = strength;
        this.stamina = stamina;
        this.agility = agility;
        this.goldReward = goldReward;
    }

    @JsonIgnore
    public StatBlock getStatBlock(){
        return new StatBlock(magic, knowledge, stamina, strength, agility);
    }

    public static Encounter generate(){
        Random random = new Random();
        int power = random.nextInt(5) + 1;
        int base = power * 30;
        int magic = base + (random.nextInt(30) - 15);
        int knowledge = base + (random.nextInt(30) - 15);
        int strength = base + (random.nextInt(30) - 15);
        int stamina = base + (random.nextInt(30) - 15);
        int agility = base + (random.nextInt(30) - 15);
        int goldReward = power * 13 + random.nextInt(13) - 7;





        return new Encounter(magic, knowledge, strength, stamina, agility, goldReward);
    }
}
