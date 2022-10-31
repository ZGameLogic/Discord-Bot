package bot.role.data.results;

import bot.role.data.structures.Player;
import bot.role.data.structures.StatBlock;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import data.serializing.SavableData;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Date;

@NoArgsConstructor
@Getter
public class EncounterFightResults extends SavableData {
    public final static String resultData = "Encounter";
    private String encounterName;
    private StatBlock encounterStats;
    private Player player;
    private int attackerPoints;
    private StatBlock rolled;
    private StatBlock total;
    private StatBlock resultStatChange;
    private int gold;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date time;

    public EncounterFightResults(long id, String encounterName, StatBlock encounterStats, Player player, int attackerPoints, StatBlock rolled, StatBlock total, StatBlock resultStatChange, int gold) {
        super(id);
        this.encounterName = encounterName;
        this.encounterStats = encounterStats;
        this.player = player;
        this.attackerPoints = attackerPoints;
        this.rolled = rolled;
        this.total = total;
        this.resultStatChange = resultStatChange;
        this.gold = gold;
        time = new Date();
    }

    @JsonIgnore
    public boolean isAttackerWon(){
        return attackerPoints >= 3;
    }
}
