package data.database.planData;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Embeddable;
import java.lang.reflect.Field;
import java.util.Date;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
public class User {

    public enum Status {
        Accepted(1),
        Declined(-1),
        Waitlist(2),
        Maybe(3),
        Waiting(0);

        Status(int value){
            try {
                Field field = this.getClass().getSuperclass().getDeclaredField("ordinal");
                field.setAccessible(true);
                field.set(this, value);
            } catch (Exception ex) {
                throw new RuntimeException("Can't update enum ordinal: " + ex);
            }
        }


    }

    private Long id;
    private Status status;
    private Long messageId;
    private Date waitlist_time;

    public User(Long id, Status status) {
        this.id = id;
        this.status = status;
    }
}
