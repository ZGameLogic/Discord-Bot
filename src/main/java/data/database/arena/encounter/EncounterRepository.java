package data.database.arena.encounter;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

@Component
public interface EncounterRepository extends JpaRepository<Encounter, Long> {

}
