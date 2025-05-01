package com.zgamelogic.data.database.cobbleData.npc;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CobbleNpcRepository extends JpaRepository<CobbleNpc, CobbleNpc.CobbleNpcId> {
    @Transactional
    void deleteAllById_UserId(long userId);
}
