package com.zgamelogic.data.database.cobbleData.history;

import com.zgamelogic.data.database.cobbleData.player.CobblePlayer;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
//@DiscriminatorColumn(name = "history_type")
@NoArgsConstructor
public abstract class CobbleHistory {
    @Id
    @GeneratedValue
    private UUID id;
    private LocalDateTime completed;

    @ManyToOne
    @JoinColumn(name = "playerId", referencedColumnName = "playerId", nullable = false)
    private CobblePlayer player;
}
