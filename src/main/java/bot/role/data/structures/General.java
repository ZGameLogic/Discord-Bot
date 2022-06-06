package bot.role.data.structures;

import data.serializing.SavableData;
import lombok.Getter;

@Getter
public class General extends SavableData {

    private int dayCount;
    private int encountersFought;
    private int challengesFought;

    public General(){
        super("general");
        dayCount = 0;
        encountersFought = 0;
        challengesFought = 0;
    }

    public General increaseDayCount(){
        dayCount++;
        return this;
    }

    public General increaseEncounterFought(){
        encountersFought++;
        return this;
    }

    public General increaseChallengesFought(){
        challengesFought++;
        return this;
    }
}
