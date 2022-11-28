package data.database.cardData.cards;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.HashMap;

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

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof CardData){
            return ((CardData) obj).getId() == id;
        } else if(obj instanceof Long){
            return id == (Long)obj;
        }else if(obj instanceof Integer){
            return id == (Integer)obj;
        }
        return false;
    }

    @Override
    public int hashCode() {
        System.out.println("called2");
        return Long.hashCode(id);
    }
}
