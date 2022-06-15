package bot.role.helpers;

import bot.role.data.item.Item;
import bot.role.data.item.Modifier;
import bot.role.data.item.ShopItem;
import bot.role.data.structures.Activity;
import bot.role.data.dungeon.saveable.Encounter;
import bot.role.data.structures.Tournament;

import java.util.LinkedList;
import java.util.List;
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

    public static ShopItem generateShopItem(){
        Random random = new Random();
        Item.Rarity rarity = Item.Rarity.random();
        Item.Material repairMat = Item.Material.random();
        List<Modifier> mods = new LinkedList<>();
        for(int i = 0; i < random.nextInt((int)((rarity.getMerit() + 1) * 10)) / 10; i++){

        }
        return null;
    }

    public static Activity generateActivity(){
        return null;
    }

    public static Tournament generateTournament(){
        return null;
    }


}
