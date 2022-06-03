package bot.role.data.item;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class Modifier {
    private Stat stat;
    private Type type;
    private int amount;

    public enum Stat {
        STRENGTH("Strength"), MAGIC("Magic"), KNOWLEDGE("Knowledge"), AGILITY("Agility"), STAMINA("Stamina"), ACTIVITY("Activity"), GOLD("Gold"),
        BLOB("Blob"), WIZARD("Wizard"), WOLF("Wolf"), BANDIT("Bandit"), GIANT("Giant"), TROLL("Troll"), GHOUL("Ghoul"), SKELETON("Skeleton");

        @Getter
        private String string;
        private Stat(String string){
            this.string = string;
        }
    }

    public enum Type {
        STATIC("Static"), ACTIVE("Active"), BANE("Bane");
        @Getter
        private String string;
        private Type(String string){
            this.string = string;
        }
    }
}
