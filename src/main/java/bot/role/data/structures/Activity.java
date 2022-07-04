package bot.role.data.structures;

import bot.role.data.jsonConfig.Strings;
import com.fasterxml.jackson.annotation.JsonFormat;
import data.serializing.DataRepository;
import data.serializing.SavableData;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Clock;
import java.time.Duration;
import java.util.Date;
import java.util.Random;

@NoArgsConstructor
@Getter
public class Activity extends SavableData {

    @Getter
    @AllArgsConstructor
    public enum Stat {
        KNOWLEDGE("Knowledge"), MAGIC("Magic"), AGILITY("Agility"), STAMINA("Stamina"), STRENGTH("Strength");
        private String stat;
        @Override
        public String toString(){
            return stat;
        }

        public static Stat random(){
            return Stat.values()[new Random().nextInt(Stat.values().length)];
        }
    }

    public enum Type {
        JOB, TRAINING;
        public static Type random(){
            int type = new Random().nextInt(2);
            if(type == 0){
                return JOB;
            }
            return TRAINING;
        }
    }

    private int gold;
    private int activityCost;
    private String activityName;
    private Type type;
    private Stat statType;
    private int statAmount;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date departs;

    public Activity(int gold, int activityCost, String activityName, Type type) {
        this.gold = gold;
        this.activityCost = activityCost;
        this.activityName = activityName;
        this.type = type;
        Clock c = Clock.systemUTC();
        c = Clock.offset(c, Duration.ofDays(6 / 2));
    }

    public Activity(int activityCost, int gold, Stat statType, int statAmount, Type type) {
        this.activityCost = activityCost;
        this.gold = gold;
        this.statType = statType;
        this.statAmount= statAmount;
        this.type = type;
    }

    public Activity(long id, int gold, int activityCost, String activityName, Stat statType, int statAmount, Type type, Date departs) {
        super(id);
        this.gold = gold;
        this.activityCost = activityCost;
        this.type = type;
        this.activityName = activityName;
        this.statType = statType;
        this.statAmount = statAmount;
        this.departs = departs;
    }

    public static Activity random(){
        Random random = new Random();
        Strings strings = new DataRepository<Strings>("arena\\strings").loadSerialized();
        Type type = Type.random();
        if(type == Type.JOB){
            int aCost = random.nextInt(2) + 1;
            int gold = (random.nextInt(5) + 10) * aCost;
            String activityName = strings.getGoldJobName();
            return new Activity(gold, aCost, activityName, type);
        } else {
            int aCost = 2;
            int gold = random.nextInt(50) - 25 + 50;
            Stat statType = Stat.random();
            int statAmount = 5;
            return new Activity(aCost, gold, statType, statAmount, type);
        }
    }
}
