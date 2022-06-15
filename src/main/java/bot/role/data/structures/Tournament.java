package bot.role.data.structures;

import com.fasterxml.jackson.annotation.JsonFormat;
import data.serializing.DataRepository;
import data.serializing.SavableData;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Clock;
import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@NoArgsConstructor
@Getter
public class Tournament extends SavableData {

    private String name;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date tournamentDate;
    private Map<Long, String> playerIds;

    public Tournament(String name, Date tournamentDate){
        playerIds = new HashMap<>();
        this.tournamentDate = tournamentDate;
        this.name = name;
    }

    public void addPlayer(long id, String name){
        playerIds.put(id, name);
    }

    public void removePlayer(long id){
        playerIds.remove(id);
    }

    public static Tournament random(){
        DataRepository<General> gRepo = new DataRepository<>("arena\\general");
        General g = gRepo.loadSerialized();
        int tournamentCount = g.getTournamentCount();
        gRepo.saveSerialized(g.increaseTournamentCount());
        Clock c = Clock.systemUTC();
        c = Clock.offset(c, Duration.ofDays(6 / 2));
        Date date = new Date(c.millis());
        return new Tournament("Shlongshot tournament #" + tournamentCount, date);
    }
}
