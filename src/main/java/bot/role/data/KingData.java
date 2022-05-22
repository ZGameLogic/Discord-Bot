package bot.role.data;

import data.serializing.SaveableData;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.LinkedList;
import java.util.List;

@EqualsAndHashCode
@Getter
public class KingData extends SaveableData {

    private long taxRoleID;
    private int taxAmount;
    private List<Long> playersFoughtKing;
    private int kingDayRun;

    public KingData() {
        super("king");
        playersFoughtKing = new LinkedList<>();
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
        taxRoleID = 0;
        taxAmount = 0;
    }
}
