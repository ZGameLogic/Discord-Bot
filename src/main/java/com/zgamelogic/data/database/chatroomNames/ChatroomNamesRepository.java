package com.zgamelogic.data.database.chatroomNames;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public interface ChatroomNamesRepository extends JpaRepository<ChatroomName, String> {
    @Query("SELECT c FROM ChatroomName c ORDER BY FUNCTION('RANDOM') LIMIT 1")
    Optional<ChatroomName> findRandom();
}
