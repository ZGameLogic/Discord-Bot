package com.zgamelogic.services;

import com.zgamelogic.bot.services.CobbleResourceService;
import com.zgamelogic.data.database.cobbleData.CobbleServiceException;
import com.zgamelogic.data.database.cobbleData.history.CobbleHistoryRepository;
import com.zgamelogic.data.database.cobbleData.npc.CobbleNpcRepository;
import com.zgamelogic.data.database.cobbleData.player.CobblePlayer;
import com.zgamelogic.data.database.cobbleData.player.CobblePlayerRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.io.File;
import java.util.UUID;

import static com.zgamelogic.data.database.cobbleData.CobbleBuildingType.*;

@Service
@Slf4j
public class CobbleService {
    private final CobblePlayerRepository cobblePlayerRepository;
    private final CobbleHistoryRepository cobbleHistoryRepository;
    private final CobbleResourceService cobbleResourceService;
    private final CobbleNpcRepository cobbleNpcRepository;

    private final File BASE_DIR;

    public CobbleService(CobblePlayerRepository cobblePlayerRepository, CobbleHistoryRepository cobbleHistoryRepository, CobbleResourceService cobbleResourceService, CobbleNpcRepository cobbleNpcRepository) {
        this.cobblePlayerRepository = cobblePlayerRepository;
        this.cobbleHistoryRepository = cobbleHistoryRepository;
        this.cobbleResourceService = cobbleResourceService;
        this.cobbleNpcRepository = cobbleNpcRepository;
        File file = new File("/cobble"); // check if we are in cluster
        if(!file.exists()) file = new File("cobble"); // change to local
        BASE_DIR = file;
        log.info("Cobble service started");
    }

    @PostConstruct
    public void init() {
        deleteCobblePlayer(232675572772372481L);
    }

    public CobblePlayer startCobblePlayer(long playerId) throws CobbleServiceException {
        if(cobblePlayerRepository.existsById(playerId)) throw new CobbleServiceException(("A cobble town already exists for this player"));
        CobblePlayer cobblePlayer = new CobblePlayer(playerId);
        String name = cobbleResourceService.randomName();
        UUID buildingUUID = UUID.randomUUID();
        cobblePlayer.addBuilding(TOWN_HALL, 1, "Town Hall", buildingUUID);
        cobblePlayer.addNpc(name.split(" ")[0], name.split(" ")[1], 0L);
        cobblePlayer.getNpcs().get(0).setCobbleBuildingId(buildingUUID);
        return cobblePlayerRepository.save(cobblePlayer);
    }

    public void deleteCobblePlayer(long playerId) {
        cobbleNpcRepository.deleteAllById_UserId(playerId);
        cobblePlayerRepository.deleteById(playerId);
    }
}
