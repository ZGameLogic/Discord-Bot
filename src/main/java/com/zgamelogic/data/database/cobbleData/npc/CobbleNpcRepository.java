package com.zgamelogic.data.database.cobbleData.npc;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CobbleNpcRepository extends JpaRepository<CobbleNpc, CobbleNpc.CobbleNpcId> {
    @Transactional
    void deleteAllById_UserId(long userId);
    List<CobbleNpc> findAllById_UserId(long userId);
    Optional<CobbleNpc> findById_UserIdAndId_Id(long idUserId, UUID idId);
}
