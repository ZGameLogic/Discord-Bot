package bot.role.data.item;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
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

}
