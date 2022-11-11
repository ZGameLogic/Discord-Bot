package data.database.planData;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Embeddable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
public class User {

    private Long id;
    private Integer status;
    private Long messageId;

    public User(Long id, Integer status) {
        this.id = id;
        this.status = status;
    }
}
