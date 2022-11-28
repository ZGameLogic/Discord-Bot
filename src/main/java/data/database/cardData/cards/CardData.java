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

    public String toDiscordMessage(boolean includeCollection){
        if(includeCollection){
            return collection + " " + String.format("__%04d__ **", id) + name + "**";
        }
        return String.format("__%04d__ **", id) + name + "**";
    }

    public int getSellback(){
        return 16 + (-(rarity - 5));
    }

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
        return Long.hashCode(id);
    }
}
