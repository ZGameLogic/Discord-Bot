package com.zgamelogic.data.database.userData;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface UserDataRepository extends JpaRepository<User, Long> {
    @Query("SELECT u FROM User u WHERE u.id IN :ids AND (u.no_hour_message IS NULL OR u.no_hour_message = TRUE)")
    List<User> findUsersWithNoHourMessageEnabled(@Param("ids") List<Long> ids);
}
