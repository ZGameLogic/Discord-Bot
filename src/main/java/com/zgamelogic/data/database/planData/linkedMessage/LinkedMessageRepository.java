package com.zgamelogic.data.database.planData.linkedMessage;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LinkedMessageRepository extends JpaRepository<LinkedMessage, LinkedMessage.LinkedMessageId> {
    @Transactional
    void deleteAllById_ChannelId(long channelId);
    boolean existsById_ChannelId(long channelId);
}
