package bot.role.data.structures;

import data.serializing.SavableData;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.LinkedList;
import java.util.List;

@EqualsAndHashCode
@Getter
public class KingData extends SavableData {

    private long taxRoleID;
    private int taxAmount;
    private List<Long> playersFoughtKing;
    private int kingDayRun;

    public KingData() {
        super("king");
        playersFoughtKing = new LinkedList<>();
        kingDayRun = 0;
    }

    /* King stuff */

    public void addPlayerKingFought(long id) {
        playersFoughtKing.add(id);
    }

    public void resetList() {
        playersFoughtKing = new LinkedList<>();
    }

    public boolean canFightKing(long id) {
        return !playersFoughtKing.contains(id);
    }

    /* Tax Stuff */

    public void setTax(long taxRoleID, int taxAmount) {
        this.taxRoleID = taxRoleID;
        this.taxAmount = taxAmount;
    }

    public void resetTax() {
        setTax(0, 0);
    }

    public void resetRun(){
        kingDayRun = 0;
    }

    public void addDayToRun(){
        kingDayRun++;
    }
}
