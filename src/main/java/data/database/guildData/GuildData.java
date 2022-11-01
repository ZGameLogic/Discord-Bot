package data.database.guildData;

import lombok.*;

import javax.persistence.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Entity
@Table(name = "Guilds")
public class GuildData {

    @Id
    @Column(name = "id")
    private long id;

    private long configChannelId, configMessageId;

    private boolean chatroomEnabled;
    private long partyCategory, afkChannelId, createChatId;
    private long limitCommandId, renameCommandId;

}
