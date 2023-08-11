package data.database.guildData;

import lombok.*;
import lombok.experimental.Accessors;

import javax.persistence.*;

@Accessors(chain = true)
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

    // devops bot stuff
    private Boolean devopsEnabled;

    // cards bot stuff
    private Boolean cardsEnabled;

    // CurseForge bot stuff
    private Boolean curseforgeEnabled;
    private Long curseforgeCommandId;

    // Hunt stuff
    private Boolean huntEnabled;
    private Long randomizerSummonId;

    // Generator stuff
    private Boolean generatorEnabled;
    private Long generateDungeonCommandId;
}
