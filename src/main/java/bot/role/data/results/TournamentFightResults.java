package bot.role.data.results;

import data.serializing.SavableData;

public class TournamentFightResults extends SavableData {
    public final static String resultData = "Tournament Fight";
    public TournamentFightResults(String id) {
        super(id);
    }
}
