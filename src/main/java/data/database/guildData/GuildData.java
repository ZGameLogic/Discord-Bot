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

    // party bot stuff
    private Boolean chatroomEnabled;
    private Long partyCategory, afkChannelId, createChatId;
    private Long limitCommandId, renameCommandId;

    // planner bot stuff
    private Boolean planEnabled;
    private Long planChannelId;
    private Long createPlanCommandId, textCommandId;
}
