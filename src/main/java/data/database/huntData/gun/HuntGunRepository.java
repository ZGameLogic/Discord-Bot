package data.database.huntData.gun;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.LinkedList;

public interface HuntGunRepository extends JpaRepository<HuntGun, String> {

    @Query(value = "SELECT * FROM hunt_guns g WHERE g.slot != 'LARGE'", nativeQuery = true)
    LinkedList<HuntGun> findAllMediumAndSmallGuns();

    @Query(value = "SELECT * FROM hunt_guns g WHERE g.slot = 'MEDIUM'", nativeQuery = true)
    LinkedList<HuntGun> findAllMediumGuns();

    @Query(value = "SELECT * FROM hunt_guns g WHERE g.slot = 'LARGE'", nativeQuery = true)
    LinkedList<HuntGun> findAllLargeGuns();

    @Query(value = "SELECT * FROM hunt_guns g WHERE g.dual_wieldable = 'true'", nativeQuery = true)
    LinkedList<HuntGun> findAllDuals();
}
