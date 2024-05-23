package com.zgamelogic.data.database.cardData.player;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import jakarta.persistence.*;
import java.util.*;

@Accessors(chain = true)
@Getter
@Setter
@NoArgsConstructor
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

    @ElementCollection
    @CollectionTable(name="player_packs")
    private Map<String, Integer> packs;

    private Long currency;
    private Long progress;
    private Date joinedVoice;

    public PlayerCardData(Long userId){
        this.userId = userId;
        deck = new LinkedList<>();
        packs = new HashMap<>();
        currency = 0l;
        progress = 0l;
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

    public void addCurrency(int amount){
        currency += amount;
    }

    public void removeCurrency(int amount){
        currency -= amount;
    }

    public boolean hasCurrency(int amount){
        return currency >= amount;
    }

    public void addProgress(long amount){
        progress += amount;
    }

    public void removeProgress(long amount){
        progress -= amount;
    }

    public boolean hasProgress(long amount){
        return progress >= amount;
    }

    public void addProgress(int amount){
        progress += amount;
    }

    public void removeProgress(int amount){
        progress -= amount;
    }

    public boolean hasProgress(int amount){
        return progress >= amount;
    }

    public void redeemPack(){
        removeProgress(3600);
        addPack();
    }

    public void addPack(){
        addPack("generic");
    }

    public void addPack(int count){
        for(int i = 0; i < count; i++) addPack("generic");
    }

    public void addPack(String collection, int count){
        for(int i = 0; i < count; i++) addPack(collection);
    }

    public void addPack(String collection){
        if(packs.containsKey(collection)){
            packs.put(collection, packs.get(collection) + 1);
        } else {
            packs.put(collection, 1);
        }
    }

    public void removePack(){
        removePack("generic");
    }

    public void removePack(String collection, int count){
        for(int i = 0; i < count; i++) removePack(collection);
    }

    public void removePack(int count){
        for(int i = 0; i < count; i++) removePack("generic");
    }

    public void removePack(String collection){
        if(packs.containsKey(collection)){
            packs.put(collection, packs.get(collection) - 1);
        }
    }

    public void removeAllPacks() {
        packs = new HashMap<>();
    }
}
