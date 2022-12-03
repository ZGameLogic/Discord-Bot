package data.database.cardData.cards;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.LinkedList;

@Component
public interface CardDataRepository extends JpaRepository<CardData, Long> {

    @Query(value = "SELECT * FROM cards c WHERE c.collection = :collection", nativeQuery = true)
    LinkedList<CardData> findCardsByCollection(@Param("collection") String collection);

    @Query(value = "SELECT * FROM cards c WHERE c.rarity = :rarity", nativeQuery = true)
    LinkedList<CardData> findCardsByRarity(@Param("rarity") int rarity);

    @Query(value = "SELECT * FROM cards c WHERE c.collection = :collection and c.rarity = :rarity", nativeQuery = true)
    LinkedList<CardData> findCardsByCollectionAndRarity(@Param("collection") String collection, @Param("rarity") int rarity);

    @Query(value = "SELECT * FROM cards c WHERE c.collection = :collection and c.name = :name", nativeQuery = true)
    LinkedList<CardData> findCardsByCollectionAndName(@Param("collection") String collection, @Param("name") String name);

    @Query(value = "SELECT DISTINCT collection FROM cards", nativeQuery = true)
    LinkedList<String> listCardCollections();

    @Query(value = "SELECT DISTINCT collection FROM cards c WHERE c.id IN :ids", nativeQuery = true)
    LinkedList<String> listCardCollectionsById(@Param("ids") Collection<Long> ids);

    @Query(value = "SELECT DISTINCT name FROM cards c WHERE c.id IN :ids AND c.collection like :collection", nativeQuery = true)
    LinkedList<String> findByCollectionAndIds(@Param("ids") Collection<Long> ids, @Param("collection") String collection);
}
