package data.database.cardData.guild;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import javax.persistence.*;

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

    private Long slashCommandId;
    private Long shopTextChannelId;
}
