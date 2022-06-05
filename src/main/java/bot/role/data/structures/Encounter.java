package bot.role.data.structures;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import data.serializing.SavableData;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Date;

@NoArgsConstructor
@Getter
public class Encounter extends SavableData {
    private int magic, knowledge, strength, stamina, agility;
    private int goldReward;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date departs;
    private String name;

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
}
