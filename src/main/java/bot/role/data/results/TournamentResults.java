package bot.role.data.results;

import data.serializing.SavableData;

public class TournamentResults extends SavableData {
    public final static String resultData = "Tournament";
    public TournamentResults(String id) {
        super(id);
    }
}
