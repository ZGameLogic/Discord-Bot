package data.database.chatroomNames;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public interface ChatroomNamesRepository extends JpaRepository<ChatroomName, String> {
    @Query(value = "SELECT TOP 1 * FROM chatroom_names ORDER BY NEWID()", nativeQuery = true)
    Optional<ChatroomName> findRandom();
}
