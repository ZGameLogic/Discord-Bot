package bot.role.data.results;

import data.serializing.SaveableData;

public class TournamentResults extends SaveableData {
    public final static String resultData = "Tournament";
    public TournamentResults(String id) {
        super(id);
    }
}
