package data.database.guildData;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface GuildDataRepository extends JpaRepository<GuildData, Long> {
    List<GuildData> findByChatroomEnabledTrue();
}
