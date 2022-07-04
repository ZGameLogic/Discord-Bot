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
import java.util.Random;

@NoArgsConstructor
@Getter
public class Tournament extends SavableData {

    private String name;
    private Map<Long, String> playerIds;
    private int entryFee;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date departs;

    public Tournament(String name, Date tournamentDate, int entryFee){
        playerIds = new HashMap<>();
        departs = tournamentDate;
        this.name = name;
        this.entryFee = entryFee;
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
        int entryFee = new Random().nextInt(30) + 25;
        return new Tournament("Shlongshot tournament #" + tournamentCount, date, entryFee);
    }
}
