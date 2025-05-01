package com.zgamelogic.data.database.cobbleData.npc;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
public class CobbleNpc {
    @Id
    private CobbleNpcId id;
    private LocalDate born;
    private String firstName;
    private String lastName;
    private long appearance;

    @JoinColumn(name = "cobbleBuildingId", referencedColumnName = "cobbleBuildingId")
    private UUID cobbleBuildingId;

    public CobbleNpc(long userId, String firstname, String lastname, long appearance) {
        id = new CobbleNpcId(userId);
        this.firstName = firstname;
        this.lastName = lastname;
        this.appearance = appearance;
        born = LocalDate.now();
    }

    @Getter
    @Embeddable
    @ToString
    @EqualsAndHashCode
    @NoArgsConstructor
    public static class CobbleNpcId {
        @GeneratedValue(strategy = GenerationType.UUID)
        public UUID id;
        private long userId;

        public CobbleNpcId(long userId) {
            this.userId = userId;
        }
    }
}
