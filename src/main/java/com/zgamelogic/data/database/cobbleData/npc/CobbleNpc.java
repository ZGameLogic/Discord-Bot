package com.zgamelogic.data.database.cobbleData.npc;

import com.zgamelogic.data.database.cobbleData.building.CobbleBuilding;
import com.zgamelogic.data.database.cobbleData.player.CobblePlayer;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
public class CobbleNpc {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private LocalDateTime born;
    private String firstName;
    private String lastName;
    private String appearance;

    @OneToOne
    @JoinColumn(name = "cobbleBuildingId", referencedColumnName = "cobbleBuildingId")
    private CobbleBuilding cobbleBuilding;

    @ManyToOne
    @JoinColumn(name = "playerId", referencedColumnName = "playerId", nullable = false)
    private CobblePlayer player;

    public CobbleNpc(CobblePlayer player, String firstname, String lastname, String appearance) {
        this.firstName = firstname;
        this.lastName = lastname;
        this.appearance = appearance;
        this.player = player;
        born = LocalDateTime.now();
    }

    public String getFullName(){
        return firstName + " " + lastName;
    }
}
