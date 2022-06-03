package bot.role.data.structures;

import com.fasterxml.jackson.annotation.JsonFormat;
import data.serializing.SaveableData;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Date;

@NoArgsConstructor
@Getter
public class Activity extends SaveableData {

    public enum Type {
        JOB, TRAINING
    }

    private int gold;
    private int activityCost;
    private String activityName;
    private Type type;
    private String statType;
    private int statAmount;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date departs;

    public Activity(long id, int gold, int activityCost, String activityName, String statType, int statAmount, Type type, Date departs) {
        super(id);
        this.gold = gold;
        this.activityCost = activityCost;
        this.type = type;
        this.departs = departs;
        this.activityName = activityName;
        this.statType = statType;
        this.statAmount = statAmount;
    }
}
