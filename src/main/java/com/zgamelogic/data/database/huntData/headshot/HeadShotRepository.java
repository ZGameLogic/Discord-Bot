package com.zgamelogic.data.database.huntData.headshot;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HeadShotRepository extends JpaRepository<HeadShot, Long> {}
