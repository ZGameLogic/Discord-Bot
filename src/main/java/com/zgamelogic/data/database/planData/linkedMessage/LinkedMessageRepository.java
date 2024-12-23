package com.zgamelogic.data.database.planData.linkedMessage;

import org.springframework.data.jpa.repository.JpaRepository;

public interface LinkedMessageRepository extends JpaRepository<LinkedMessage, LinkedMessage.LinkedMessageId> {
    void deleteAllByChannelId(long channelId);
}
