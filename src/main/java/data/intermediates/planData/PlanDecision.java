package data.intermediates.planData;

import data.database.planData.User;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.util.HashMap;

import static data.database.planData.User.Status.*;

@AllArgsConstructor
@Getter
@EqualsAndHashCode
public class PlanDecision {
    private boolean full;
    private boolean needsFillIn;
    private User.Status status;

    public static HashMap<PlanDecision, ActionRow> decisionTree(){
        HashMap<PlanDecision, ActionRow> decisionTree = new HashMap<>();

        decisionTree.put(new PlanDecision(true, true, DECIDING), ActionRow.of(
                Button.secondary("waitlist_event", "Waitlist"),
                Button.danger("deny_event", "Deny")
        ));

        decisionTree.put(new PlanDecision(true, true, ACCEPTED), ActionRow.of(
                Button.secondary("waitlist_event", "Waitlist"),
                Button.danger("deny_event", "Deny")
        ));

        decisionTree.put(new PlanDecision(true, true, MAYBED), ActionRow.of(
                Button.secondary("waitlist_event", "Waitlist"),
                Button.danger("deny_event", "Deny")
        ));

        decisionTree.put(new PlanDecision(true, true, WAITLISTED), ActionRow.of(
                Button.secondary("waitlist_event", "Waitlist"),
                Button.danger("deny_event", "Deny")
        ));

        decisionTree.put(new PlanDecision(true, true, FILLINED), ActionRow.of(
                Button.secondary("waitlist_event", "Waitlist"),
                Button.danger("deny_event", "Deny")
        ));

        decisionTree.put(new PlanDecision(true, true, DECLINED), ActionRow.of(
                Button.secondary("waitlist_event", "Waitlist"),
                Button.danger("deny_event", "Deny")
        ));

        return decisionTree;
    }
}
