package data.database.cardData.guild;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Embeddable;
import java.util.LinkedList;
import java.util.List;

@Embeddable
@Getter
@Setter
public class PlayerCardData {

    @Column
    @ElementCollection
    private List<Long> deck;
    private Long currency;

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
