package data.database.curseforge;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.util.Date;

@Accessors(chain = true)
@Getter
@Setter
@NoArgsConstructor
@ToString
@Entity
@Table(name = "curseforge")
public class CurseforgeRecord {
    @Id
    @GeneratedValue
    private Long id;

    private Long channelId;
    private Long guildId;
    private String projectId;
    private String projectVersionId;
    private Date lastChecked;
}
