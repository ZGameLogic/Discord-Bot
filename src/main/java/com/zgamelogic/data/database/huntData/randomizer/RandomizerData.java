package com.zgamelogic.data.database.huntData.randomizer;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "hunt_randomizers")
public class RandomizerData {

    @Id
    private long messageId;
    private long playerId;
    private long channelId;
    private long guildId;
    
}
