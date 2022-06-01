package bot.role.data.results;

import bot.role.data.structures.Encounter;
import bot.role.data.structures.Player;
import bot.role.data.structures.StatBlock;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import data.serializing.SaveableData;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Date;

@NoArgsConstructor
@Getter
public class EncounterFightResults extends SaveableData {
    public final static String resultData = "Encounter";
    private Encounter encounter;
    private Player player;
    private int attackerPoints;
    private StatBlock rolled;
    private StatBlock total;
    private StatBlock resultStatChange;
    private int gold;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date time;

    public EncounterFightResults(long id, Encounter encounter, Player player, int attackerPoints, StatBlock rolled, StatBlock total, StatBlock resultStatChange, int gold) {
        super(id);
        this.encounter = encounter;
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
