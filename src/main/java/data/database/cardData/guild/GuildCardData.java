package data.database.cardData.guild;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.util.Map;

@Accessors(chain = true)
@Getter
@Setter
@NoArgsConstructor
@ToString
@Entity
@Table(name = "Guild_Card")
public class GuildCardData {

    @Id
    @Column(name = "id")
    private long id;

    @ElementCollection
    @MapKeyColumn(name="user_id")
    @CollectionTable(name="player_card_data", joinColumns=@JoinColumn(name="guild_id"))
    private Map<Long, PlayerCardData> players;

    private Long tradeSlashCommandId;
}
