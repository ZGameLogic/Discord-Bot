package data.database.onlineData;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.dv8tion.jda.api.entities.Member;

import java.util.Date;

@Entity
@Table(name = "user_record")
@Getter
@NoArgsConstructor
public class Record {

    @Id
    @GeneratedValue
    private Long id;
    private long discordId;
    private String discordName;
    private Date date;

    public Record(Member member){
        
    }
}
