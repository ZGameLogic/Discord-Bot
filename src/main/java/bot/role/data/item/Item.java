package bot.role.data.item;

import lombok.*;
import net.minidev.json.annotate.JsonIgnore;

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

    private List<Modifier> modifiers;
    private String name;
    private String description;
    private Rarity rarity;
    private int maxUses;
    private int uses;

    /**
     * Creates an item with one modifier
     * @param name name of the item
     * @param description description of the item
     * @param rarity rarity of the item
     * @param stat stat the item effects
     * @param type type of the item
     * @param amount amount of the stat the item effects
     */
    public Item (String name, String description, Rarity rarity, Modifier.Stat stat, Modifier.Type type, int amount, int maxUses){
        Modifier modifier = new Modifier(stat, type, amount);
        modifiers = new LinkedList<>();
        modifiers.add(modifier);
        this.name = name;
        this.description = description;
        this.rarity = rarity;
        this.maxUses = maxUses;
        uses = 0;
    }

    public void useItem(){
        uses++;
    }

    public boolean broken(){
        return uses <= maxUses;
    }

}
