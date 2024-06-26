package com.zgamelogic.data.database.huntData.randomizer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public interface HuntRandomizerRepository extends JpaRepository<RandomizerData, Long> {

    @Query(value = "SELECT * FROM hunt_randomizers r WHERE r.player_id = :uid", nativeQuery = true)
    Optional<RandomizerData> getByUserId(@Param("uid") String uid);
}
