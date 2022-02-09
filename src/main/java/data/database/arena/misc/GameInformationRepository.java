package data.database.arena.misc;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

@Component
public interface GameInformationRepository extends JpaRepository<GameInformation, String> {

}
