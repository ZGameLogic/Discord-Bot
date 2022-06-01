package bot.role.data.results;

import com.fasterxml.jackson.annotation.JsonFormat;
import data.serializing.SaveableData;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Date;

@NoArgsConstructor
@Getter
@ToString
public class ActivityResults extends SaveableData {

    public final static String resultData = "Activity";

    private String playerName;
    private String reward;
    private int rewardAmount;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date time;

    public ActivityResults(long id, String playerName, String reward, int rewardAmount) {
        super(id);
        this.playerName = playerName;
        this.reward = reward;
        this.rewardAmount = rewardAmount;
        time = new Date();
    }
}
