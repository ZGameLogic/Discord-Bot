package data.database.planData;

import lombok.*;

import javax.persistence.*;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Entity
@Table(name = "Plans")
public class Plan {
    @Id
    @Column(name = "id")
    private long id;

    @ElementCollection
    @MapKeyColumn(name="Plan_Player_Status_Id")
    @Column(name="status")
    @CollectionTable(name="Plan_Player_Status", joinColumns=@JoinColumn(name="example_id"))
    private Map<String, String> attributes;

    private String title;
    private String notes;
    private Long authorId;
    private Integer count;
}
