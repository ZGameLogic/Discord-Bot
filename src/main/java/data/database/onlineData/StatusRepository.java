package data.database.onlineData;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

@Component
public interface StatusRepository extends JpaRepository<Record, Long> {
}
