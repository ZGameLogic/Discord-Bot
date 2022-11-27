package data.database.cardData.player;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

@Accessors(chain = true)
@Getter
@Setter
@ToString
@Entity
@Table(name = "card_player")
public class PlayerCardData {

    @Id
    @Column(name = "user_id")
    private long userId;

    @ElementCollection
    @CollectionTable(name="decks")
    private List<Long> deck;

    private Long currency;
    private Long progress;
    private Date joinedVoice;

    public PlayerCardData(){
        deck = new LinkedList<>();
    }

    public void addCard(long cardId){
        deck.add(cardId);
    }

    public void removeCard(long cardId){
        deck.remove(cardId);
    }

    public boolean hasCard(long cardId){
        return deck.contains(cardId);
    }

    public void addCurrency(long amount){
        currency += amount;
    }

    public void removeCurrency(long amount){
        currency -= amount;
    }

    public boolean hasCurrency(long amount){
        return currency >= amount;
    }

}
