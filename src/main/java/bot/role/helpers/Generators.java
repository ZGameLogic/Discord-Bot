package bot.role.helpers;

import bot.role.data.item.Item;
import bot.role.data.structures.Activity;
import bot.role.data.dungeon.saveable.Encounter;

import java.util.Random;

public abstract class Generators {

    public static Encounter generateEncounter(int statMax, int statMin){
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

    public static Item generateItem(){
        return null;
    }

    public static Activity generateActivity(){
        return null;
    }


}
