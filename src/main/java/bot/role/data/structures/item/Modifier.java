package bot.role.data.structures.item;

import lombok.*;

import java.util.Arrays;
import java.util.Random;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class Modifier {
    private Stat stat;
    private Type type;
    private int amount;

    @AllArgsConstructor
    public enum Stat {
        STRENGTH("Strength"), MAGIC("Magic"), KNOWLEDGE("Knowledge"), AGILITY("Agility"), STAMINA("Stamina"), ACTIVITY("Activity"), GOLD("Gold"),
        BLOB("Blob"), WIZARD("Wizard"), WOLF("Wolf"), BANDIT("Bandit"), GIANT("Giant"), TROLL("Troll"), GHOUL("Ghoul"), SKELETON("Skeleton");
        @Getter
        private String string;

        public static Stat random(Type type){
            Stat[] stats = new Stat[0];
            switch (type){
                case STATIC:
                    stats = Arrays.copyOfRange(Stat.values(), 0, 6);
                    break;
                case ACTIVE:
                    stats = Arrays.copyOfRange(Stat.values(), 0, 4);
                    stats =  Arrays.copyOf(stats, stats.length + 1);
                    stats[stats.length - 1] = Stat.values()[6];
                    break;
                case BANE:
                    stats = Arrays.copyOfRange(Stat.values(), 7, 14);
                    break;
            }
            return stats[new Random().nextInt(stats.length)];
        }
    }

    @AllArgsConstructor
    public enum Type {
        STATIC("Static"), ACTIVE("Active"), BANE("Bane");
        @Getter
        private String string;

        public static Type random(){
            return Type.values()[new Random().nextInt(Type.values().length)];
        }
    }

    public static Modifier random(Item.Rarity rarity){
        Type type = Type.random();
        Stat stat = Stat.random(type);

        int amount;
        switch(type){
            case STATIC:
                amount = (int)((new Random().nextInt(5) + 6) * rarity.getMerit());
                break;
            case ACTIVE:
                amount = (int)((new Random().nextInt(2) + 1) * rarity.getMerit());
                break;
            default:
                amount = 0;
        }
        return new Modifier(stat, type, amount);
    }
}
