package bot.utils;

import data.database.planData.User;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.util.LinkedList;

public class PlanHelper {

    public static LinkedList<Button> getButtons(boolean full, boolean needsFillIn, User.Status status, boolean requestedFillIn){
        LinkedList<Button> neededButtons = new LinkedList<>();

        Button accept = Button.success("accept_event", "Accept");
        Button deny = Button.danger("deny_event", "Deny");
        Button maybe = Button.primary("maybe_event", "Maybe");
        Button waitlist = Button.secondary("waitlist_event", "Waitlist");
        Button dropout = Button.danger("drop_out_event", "Drop out");
        Button requestFillIn = Button.primary("request_fill_in", "Request fill in");
        Button fillIn = Button.secondary("fill_in", "Fill in");

        switch(status){
            case DECIDING:
                if(full) {
                    if(needsFillIn){
                        neededButtons.add(fillIn);
                        neededButtons.add(maybe);
                        neededButtons.add(deny);
                    } else {
                        neededButtons.add(waitlist);
                        neededButtons.add(maybe);
                        neededButtons.add(deny);
                    }
                } else {
                    neededButtons.add(accept);
                    neededButtons.add(maybe);
                    neededButtons.add(deny);
                }
                break;
            case ACCEPTED:
                if(!requestedFillIn) neededButtons.add(requestFillIn);
                neededButtons.add(dropout);
                break;
            case MAYBED:
                if(full) {
                    if(needsFillIn){
                        neededButtons.add(fillIn);
                        neededButtons.add(deny);
                    } else {
                        neededButtons.add(waitlist);
                        neededButtons.add(deny);
                    }
                } else {
                    neededButtons.add(accept);
                    neededButtons.add(deny);
                }
                break;
            case WAITLISTED:
            case FILLINED:
                neededButtons.add(deny);
                break;
            case DECLINED:
                break;
        }

        return neededButtons;
    }
}
