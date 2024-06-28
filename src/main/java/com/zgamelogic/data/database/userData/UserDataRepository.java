package com.zgamelogic.data.database.userData;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

@Component
public interface UserDataRepository extends JpaRepository<User, Long> {
}
