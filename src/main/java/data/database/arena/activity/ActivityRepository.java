package data.database.arena.activity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

@Component
public interface ActivityRepository extends JpaRepository<Activity, Long> {

}
