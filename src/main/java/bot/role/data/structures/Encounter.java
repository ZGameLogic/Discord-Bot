package bot.role.data.structures;

import data.serializing.SaveableData;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class Encounter extends SaveableData {
    private int magic, knowledge, strength, stamina, agility;
    private int goldReward;
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
    public Encounter(long id, int magic, int knowledge, int strength, int stamina, int agility, int goldReward, String name) {
        super(id);
        this.magic = magic;
        this.knowledge = knowledge;
        this.strength = strength;
        this.stamina = stamina;
        this.agility = agility;
        this.goldReward = goldReward;
        this.name = name;
    }

    public Encounter(int magic, int knowledge, int strength, int stamina, int agility, int goldReward) {
        this.magic = magic;
        this.knowledge = knowledge;
        this.strength = strength;
        this.stamina = stamina;
        this.agility = agility;
        this.goldReward = goldReward;
    }
}
