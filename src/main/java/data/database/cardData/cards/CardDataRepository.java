package data.database.cardData.cards;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface CardDataRepository extends JpaRepository<CardData, Long> {

    @Query(value = "SELECT * FROM cards c WHERE c.collection = :collection", nativeQuery = true)
    List<CardData> findCardsByCollection(@Param("collection") String collection);

    @Query(value = "SELECT * FROM cards c WHERE c.rarity = :rarity", nativeQuery = true)
    List<CardData> findCardsByRarity(@Param("rarity") int rarity);

    @Query(value = "SELECT * FROM cards c WHERE c.collection = :collection and c.rarity = :rarity", nativeQuery = true)
    List<CardData> findCardsByCollectionAndRarity(@Param("collection") String collection, @Param("rarity") int rarity);

    @Query(value = "SELECT DISTINCT collection FROM cards", nativeQuery = true)
    List<String> listCardCollections();
}
