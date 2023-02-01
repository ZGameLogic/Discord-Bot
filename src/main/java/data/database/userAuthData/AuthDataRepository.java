package data.database.userAuthData;

import data.database.devopsData.DevopsData;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthDataRepository extends JpaRepository<AuthData, Long> {
}
