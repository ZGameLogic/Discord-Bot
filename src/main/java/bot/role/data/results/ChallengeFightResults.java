package bot.role.data.results;

import bot.role.data.structures.Player;
import bot.role.data.structures.StatBlock;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import data.serializing.SaveableData;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Date;

@NoArgsConstructor
@Getter
@ToString
public class ChallengeFightResults extends SaveableData {
    public final static String resultData = "Challenge";
    private Player attacker;
    private Player defender;
    private int attackerPoints;
    private StatBlock rolled;
    private StatBlock total;
    private StatBlock resultStatChange;
    private int gold;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date time;

    public ChallengeFightResults(long id, Player attacker, Player defender, int attackerPoints, StatBlock rolled, StatBlock total, StatBlock resultStatChange, int gold) {
        super(id);
        this.attacker = attacker;
        this.defender = defender;
        this.attackerPoints = attackerPoints;
        this.rolled = rolled;
        this.total = total;
        this.resultStatChange = resultStatChange;
        this.gold = gold;
        time = new Date();
    }

    @JsonIgnore
    public boolean isAttackerWin(){
        return attackerPoints >= 3;
    }
}
