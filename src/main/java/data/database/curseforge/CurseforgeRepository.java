package data.database.curseforge;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

@Component
public interface CurseforgeRepository extends JpaRepository<CurseforgeRecord, Long> {
}
