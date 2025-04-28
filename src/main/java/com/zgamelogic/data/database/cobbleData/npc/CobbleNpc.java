package com.zgamelogic.data.database.cobbleData.npc;

import com.zgamelogic.data.database.cobbleData.building.CobbleBuilding;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Getter
public class CobbleNpc {
    @Id
    private CobbleNpcId id;
    private LocalDate born;
    private String firstName;
    private String lastName;
    private long appearance;

    @OneToOne
    @JoinColumns({
        @JoinColumn(name = "userId", referencedColumnName = "userId", insertable = false, updatable = false),
        @JoinColumn(name = "cobbleBuildingId", referencedColumnName = "cobbleBuildingId", insertable = false, updatable = false)
    })
    private CobbleBuilding building;

    @Getter
    @Embeddable
    @EqualsAndHashCode
    public static class CobbleNpcId {
        @GeneratedValue(strategy = GenerationType.UUID)
        public UUID id;
        private long userId;
    }
}
