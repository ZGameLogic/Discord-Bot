package com.zgamelogic.data.database.cardData.guild;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

@Component
public interface GuildCardDataRepository extends JpaRepository<GuildCardData, Long> {
}
