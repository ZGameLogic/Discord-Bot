package com.zgamelogic.data.database.cardData.cards;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.LinkedList;

@Component
public interface CardDataRepository extends JpaRepository<CardData, Long> {

    @Query("SELECT c FROM CardData c WHERE c.collection = :collection")
    LinkedList<CardData> findCardsByCollection(@Param("collection") String collection);

    @Query("SELECT c FROM CardData c WHERE c.rarity = :rarity")
    LinkedList<CardData> findCardsByRarity(@Param("rarity") int rarity);

    @Query("SELECT c FROM CardData c WHERE c.collection = :collection AND c.rarity = :rarity")
    LinkedList<CardData> findCardsByCollectionAndRarity(@Param("collection") String collection, @Param("rarity") int rarity);

    @Query("SELECT c FROM CardData c WHERE c.collection = :collection AND c.name = :name")
    LinkedList<CardData> findCardsByCollectionAndName(@Param("collection") String collection, @Param("name") String name);

    @Query("SELECT DISTINCT c.collection FROM CardData c")
    LinkedList<String> listCardCollections();

    @Query("SELECT DISTINCT c.collection FROM CardData c WHERE c.id IN :ids")
    LinkedList<String> listCardCollectionsById(@Param("ids") Collection<Long> ids);

    @Query("SELECT DISTINCT c.name FROM CardData c WHERE c.id IN :ids AND c.collection LIKE :collection")
    LinkedList<String> findByCollectionAndIds(@Param("ids") Collection<Long> ids, @Param("collection") String collection);
}
