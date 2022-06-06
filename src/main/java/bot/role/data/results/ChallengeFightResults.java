package bot.role.data.results;

import bot.role.data.structures.Player;
import bot.role.data.structures.StatBlock;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import data.serializing.SavableData;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Date;

@NoArgsConstructor
@Getter
@ToString
public class ChallengeFightResults extends SavableData {
    public final static String resultData = "Challenge";
    private Player attacker;
    private Player defender;
    private int attackerPoints;
    private StatBlock rolled;
    private StatBlock resultStatChange;
    private String attackerRole;
    private String defenderRole;
    private int paddingMultiplier;
    private int defenderPaddingLevel;
    private int gold;
    private boolean attackingUp;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date time;

    /**
     *
     * @param id
     * @param attacker
     * @param defender
     * @param attackerPoints
     * @param rolled
     * @param resultStatChange
     * @param attackerRole
     * @param defenderRole
     * @param paddingMultiplier
     * @param defenderPaddingLevel
     * @param gold
     */
    public ChallengeFightResults(long id, Player attacker, Player defender, int attackerPoints, StatBlock rolled, StatBlock resultStatChange, String attackerRole, String defenderRole, int paddingMultiplier, int defenderPaddingLevel, int gold, boolean attackingUp) {
        super(id);
        this.attacker = attacker;
        this.defender = defender;
        this.attackerPoints = attackerPoints;
        this.rolled = rolled;
        this.resultStatChange = resultStatChange;
        this.gold = gold;
        this.defenderRole = defenderRole;
        this.attackerRole = attackerRole;
        this.paddingMultiplier = paddingMultiplier;
        this.defenderPaddingLevel = defenderPaddingLevel;
        this.attackingUp = attackingUp;
        time = new Date();
    }

    @JsonIgnore
    public boolean isAttackerWin(){
        return attackerPoints >= 3;
    }

    public double attackerWinPercentage() {
        final boolean[][] outcomes = {
                {true, true, true, true, true},
                {true, true, true, true, false},
                {true, true, true, false, true},
                {true, true, false, true, true},
                {true, false, true, true, true},
                {false, true, true, true, true},
                {true, true, true, false, false},
                {true, true, false, true, false},
                {true, false, true, true, false},
                {false, true, true, true, false},
                {true, true, false, false, true},
                {true, false, true, false, true},
                {false, true, true, false, true},
                {true, false, false, true, true},
                {false, true, false, true, true},
                {false, false, true, true, true}
        };

        double s1w = (double) attacker.getMagicStat() / (attacker.getMagicStat() + defender.getMagicStat());
        double s1l = 1 - s1w;
        double s2w = (double) attacker.getStrengthStat() / (attacker.getStrengthStat() + defender.getStrengthStat());
        double s2l = 1 - s2w;
        double s3w = (double) attacker.getAgilityStat() / (attacker.getAgilityStat() + defender.getAgilityStat());
        double s3l = 1 - s3w;
        double s4w = (double) attacker.getKnowledgeStat() / (attacker.getKnowledgeStat() + defender.getKnowledgeStat());
        double s4l = 1 - s4w;
        double s5w = (double) attacker.getStaminaStat() / (attacker.getStaminaStat() + defender.getStaminaStat());
        double s5l = 1 - s5w;

        double totalProbability = 0.0;

        for(boolean[] line : outcomes) {
            totalProbability +=
                    (line[0] ? s1w : s1l) *
                            (line[1] ? s2w : s2l) *
                            (line[2] ? s3w : s3l) *
                            (line[3] ? s4w : s4l) *
                            (line[4] ? s5w : s5l);
        }

        return totalProbability * 100;
    }

    @JsonIgnore
    public int getTotalDefenderStatPadding(){
        return defenderPaddingLevel * paddingMultiplier;
    }
}
