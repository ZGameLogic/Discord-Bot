package com.zgamelogic.data.database.cobbleData.npc;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CobbleNpcRepository extends JpaRepository<CobbleNpc, UUID> {
    List<CobbleNpc> findAllByPlayer_PlayerId(long userId);
    Optional<CobbleNpc> findByPlayer_PlayerIdAndId(long idUserId, UUID idId);
}
