package data.database.arena.player;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

@Component
public interface PlayerRepository extends JpaRepository<Player, Long> {
	

}
