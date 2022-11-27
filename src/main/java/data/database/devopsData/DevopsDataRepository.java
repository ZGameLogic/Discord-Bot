package data.database.devopsData;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

@Component
public interface DevopsDataRepository extends JpaRepository<DevopsData, Long> {
}
