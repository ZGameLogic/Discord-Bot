package data.database.cardData.cards;

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
@Table(name = "cards")
public class CardData {

    @Id
    @Column(name = "id")
    private long id;

    private String collection;
    private Integer rarity;
    private String name;
}
