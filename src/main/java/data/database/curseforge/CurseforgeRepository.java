package data.database.curseforge;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public interface CurseforgeRepository extends JpaRepository<CurseforgeRecord, Long> {

    @Query(value = "select * from curseforge c where c.project_id = :id AND c.guild_id = :guild AND c.channel_id = :channel", nativeQuery = true)
    Optional<CurseforgeRecord> getProjectById(@Param("id") String id, @Param("guild") Long guild, @Param("channel") Long channel);
}
