package bot.role.data.structures.item;

import bot.role.data.jsonConfig.Strings;
import data.serializing.DataRepository;
import lombok.*;

import java.util.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class Item {
    @Getter
    @AllArgsConstructor
    public enum Rarity {
        MYTHIC("Mythic", 5.0, 4.0),
        LEGENDARY("Legendary", 7.5, 3.5),
        EPIC("Epic", 10.0, 3.0),
        RARE("Rare", 15.0, 2.5),
        UNCOMMON("Uncommon", 20.0, 2.0),
        COMMON("Common", 25.0, 1.0);

        private String string;
        private double rollChance;
        private double merit;

        public static Rarity random(){
            double total = getTotalValue();
            double roll = new Random().nextInt((int)Math.ceil(total));
            for(Rarity r : Rarity.values()){
                if(roll > total - r.getRollChance()){
                    return r;
                } else {
                    total -= r.getRollChance();
                }
            }
            return COMMON;
        }

        private static double getTotalValue(){
            double total = 0;
            for(Rarity r : Rarity.values()){
                total += r.getRollChance();
            }
            return total;
        }
    }

    public enum Material {
        WOOD,
        STEEL,
        IRON,
        BONE;

        public static Material random(){
            return Material.values()[new Random().nextInt(Material.values().length)];
        }
    }

    private List<Modifier> modifiers;
    private String name;
    private String description;
    private Rarity rarity;
    private Material repairMat;
    private int maxDurability;
    private int durability;

    /**
     * Creates an item with one modifier
     * @param name name of the item
     * @param description description of the item
     * @param rarity rarity of the item
     * @param stat stat the item effects
     * @param type type of the item
     * @param amount amount of the stat the item effects
     */
    public Item (String name, String description, Rarity rarity, Modifier.Stat stat, Modifier.Type type, int amount, int maxDurability, Material repairMat){
        Modifier modifier = new Modifier(stat, type, amount);
        modifiers = new LinkedList<>();
        modifiers.add(modifier);
        this.name = name;
        this.description = description;
        this.rarity = rarity;
        this.maxDurability = maxDurability;
        durability = maxDurability;
        this.repairMat = repairMat;
    }

    public static Item random(){
        Random random = new Random();
        Strings strings = new DataRepository<Strings>("arena\\strings").loadSerialized();
        Rarity rarity = Rarity.random();
        Material repairMat = Material.random();
        List<Modifier> mods = new LinkedList<>();
        int offset = random.nextInt(3) - 1;
        double randomChance = rarity.getMerit() + offset;
        if (randomChance == 0) randomChance++;
        int modCount = (int)Math.round(random.nextInt((int)(randomChance * 10)) / 10.0);
        if (modCount == 0) modCount++;
        for(int i = 0; i < modCount; i++){
            mods.add(Modifier.random(rarity));
        }
        String[] nameDesc = strings.getItemName(mods.get(random.nextInt(mods.size())));
        String name = nameDesc[0];
        String description = nameDesc[1];
        int maxDurability = (int)(4 * rarity.getMerit());
        return new Item(mods, name, description, rarity, repairMat, maxDurability, maxDurability);
    }

    public void useItem(){
        durability--;
    }

    public void repair(){
        durability = maxDurability;
    }

    /**
     * @return true if the item has 0 durability
     */
    public boolean broken(){
        return durability <= 0;
    }

}
