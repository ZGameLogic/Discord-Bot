package data.database.devopsData;

import lombok.*;
import lombok.experimental.Accessors;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Accessors(chain = true)
@Getter
@Setter
@NoArgsConstructor
@ToString
@Entity
@Table(name = "AtlassianDevops")
public class DevopsData {

    @Id
    @Column(name = "id")
    private long id;

    private String jiraURL, bitbucketURL, bambooURL;
    private String jiraPAT, bitbucketPAT, bambooPAT;

    private Long devopsCatId;

    private Long devopsGeneralChatId, devopsGeneralTextId;

    private Long createBranchSlashId;
}
