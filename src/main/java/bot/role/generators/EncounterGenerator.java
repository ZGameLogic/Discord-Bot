package bot.role.generators;


import bot.role.dungeon.saveable.Encounter;

import java.util.Random;

public abstract class EncounterGenerator {
    public static Encounter generate(int statMax, int statMin){
        Random random = new Random();
        int magic = random.nextInt(statMax - statMin) + statMin;
        int strength = random.nextInt(statMax - statMin) + statMin;
        int stamina = random.nextInt(statMax - statMin) + statMin;
        int agility = random.nextInt(statMax - statMin) + statMin;
        int knowledge = random.nextInt(statMax - statMin) + statMin;
        int gold  = random.nextInt(statMax - statMin) + statMin;
        Encounter e = new Encounter(magic, knowledge, strength, stamina, agility, gold);
        return e;
    }
}
