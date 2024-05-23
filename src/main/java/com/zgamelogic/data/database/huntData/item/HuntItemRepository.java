package com.zgamelogic.data.database.huntData.item;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.LinkedList;

public interface HuntItemRepository extends JpaRepository<HuntItem, String> {

    @Query(value = "SELECT * FROM hunt_items i WHERE i.type = :type", nativeQuery = true)
    LinkedList<HuntItem> findItemsByType(@Param("type") String type);

    @Query(value = "SELECT * FROM hunt_items i WHERE i.type != 'CONSUMABLE' and i.type != 'HEALING'", nativeQuery = true)
    LinkedList<HuntItem> findAllTools();

    @Query(value = "SELECT * FROM hunt_items i WHERE i.type = 'CONSUMABLE' or i.type = 'HEALING'", nativeQuery = true)
    LinkedList<HuntItem> findAllConsumables();
}
