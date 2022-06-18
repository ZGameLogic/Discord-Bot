package bot.role.data.dungeon.saveable;

import bot.role.data.structures.StatBlock;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.minidev.json.annotate.JsonIgnore;

import java.util.Random;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class Encounter {
    private int magic, knowledge, strength, stamina, agility;
    private int goldReward;

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
