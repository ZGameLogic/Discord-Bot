package bot.role.data.item;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Modifier {
    private Stat stat;
    private int amount;

    public enum Stat {
        STRENGTH, MAGIC, KNOWLEDGE, AGILITY, STAMINA,
        ACTIVITY
    }
}
