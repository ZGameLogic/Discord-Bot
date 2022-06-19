package bot.role.data.structures;

import bot.role.data.jsonConfig.Strings;
import bot.role.data.structures.item.Modifier;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import data.serializing.DataRepository;
import data.serializing.SavableData;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Clock;
import java.time.Duration;
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
        Modifier.Stat baneType = Modifier.Stat.values()[random.nextInt(8) + 7];
        Clock c = Clock.systemUTC();
        c = Clock.offset(c, Duration.ofDays(6 / 2));
        Date departs = new Date(c.millis());
        String name = baneType.getString();
        return new Encounter(magic, knowledge, strength, stamina, agility, goldReward, departs, name, baneType);
    }
}
