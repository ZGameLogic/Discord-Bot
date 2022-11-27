package data.database.cardData;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
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
@Table(name = "Cards")
public class CardData {

    @Id
    @Column(name = "id")
    private long id;

    private String collection;
    private int rarity;
    private String name;
    private boolean holographic;
}
