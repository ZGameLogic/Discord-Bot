package bot.role.data.item;

import lombok.*;

import java.util.LinkedList;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class Item {

    public enum Rarity {
        MYTHIC,
        LEGENDARY,
        EPIC,
        RARE,
        UNCOMMON,
        COMMON
    }

    public enum Material {
        WOOD,
        STEEL,
        IRON,
        BONE
    }

    private List<Modifier> modifiers;
    private String name;
    private String description;
    private Rarity rarity;
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
    public Item (String name, String description, Rarity rarity, Modifier.Stat stat, Modifier.Type type, int amount, int maxDurability){
        Modifier modifier = new Modifier(stat, type, amount);
        modifiers = new LinkedList<>();
        modifiers.add(modifier);
        this.name = name;
        this.description = description;
        this.rarity = rarity;
        this.maxDurability = maxDurability;
        durability = 0;
    }

    public void useItem(){
        durability--;
    }

    public void repair(){
        durability = maxDurability;
    }

    public boolean broken(){
        return durability <= 0;
    }

}
