package data.database.huntData.randomizer;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "hunt_randomizers")
public class RandomizerData {

    @Id
    private long messageId;
    private long playerId;
    private long channelId;
    private long guildId;
    
}
