package com.zgamelogic.services;

import com.zgamelogic.bot.services.CobbleResourceService;
import com.zgamelogic.data.database.cobbleData.CobbleServiceException;
import com.zgamelogic.data.database.cobbleData.history.CobbleHistoryRepository;
import com.zgamelogic.data.database.cobbleData.npc.CobbleNpc;
import com.zgamelogic.data.database.cobbleData.npc.CobbleNpcRepository;
import com.zgamelogic.data.database.cobbleData.player.CobblePlayer;
import com.zgamelogic.data.database.cobbleData.player.CobblePlayerRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.Random;
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
    private final CobbleNpcIdRepository cobbleNpcIdRepository;

    public CobbleService(CobblePlayerRepository cobblePlayerRepository, CobbleHistoryRepository cobbleHistoryRepository, CobbleResourceService cobbleResourceService, CobbleNpcRepository cobbleNpcRepository, CobbleNpcIdRepository cobbleNpcIdRepository) {
        this.cobblePlayerRepository = cobblePlayerRepository;
        this.cobbleHistoryRepository = cobbleHistoryRepository;
        this.cobbleResourceService = cobbleResourceService;
        this.cobbleNpcRepository = cobbleNpcRepository;
        File file = new File("/cobble"); // check if we are in cluster
        if(!file.exists()) file = new File("cobble"); // change to local
        BASE_DIR = file;
        log.info("Cobble service started");
        this.cobbleNpcIdRepository = cobbleNpcIdRepository;
    }

    @PostConstruct
    public void init() {
        deleteCobblePlayer(232675572772372481L);
    }

    public CobblePlayer startCobblePlayer(long playerId) throws CobbleServiceException {
        if(cobblePlayerRepository.existsById(playerId)) throw new CobbleServiceException(("A cobble town already exists for this player"));
        CobblePlayer cobblePlayer = new CobblePlayer(playerId);
        UUID buildingUUID = UUID.randomUUID();
        cobblePlayer.addBuilding(TOWN_HALL, 1, "Town Hall", buildingUUID);
        cobblePlayer.addNpc(generateRandomCobbleNpc(playerId));
        cobblePlayer.addNpc(generateRandomCobbleNpc(playerId));
        cobblePlayer.getNpcs().get(0).setCobbleBuildingId(buildingUUID);
        return cobblePlayerRepository.save(cobblePlayer);
    }

    public void deleteCobblePlayer(long playerId) {
        cobbleNpcRepository.deleteAllById_UserId(playerId);
        cobblePlayerRepository.deleteById(playerId);
    }

    public List<CobbleNpc> getCobbleNpcs(long playerId) throws CobbleServiceException {
        if(!cobblePlayerRepository.existsById(playerId)) throw new CobbleServiceException("You must start the game first with the `/cobble start` slash command");
        return cobbleNpcRepository.findAllById_UserId(playerId);
    }

    public CobbleNpc getCobbleNpc(long playerId, UUID npcId) throws CobbleServiceException {
        if(!cobblePlayerRepository.existsById(playerId)) throw new CobbleServiceException("You must start the game first with the `/cobble start` slash command");
        Optional<CobbleNpc> npc = cobbleNpcRepository.findById_UserIdAndId_Id(playerId, npcId);
        if(npc.isPresent()) return npc.get();
        throw new CobbleServiceException("Cobble npc not found");
    }

    private CobbleNpc generateRandomCobbleNpc(long id){
        Random rand = new Random();
        boolean male = rand.nextBoolean();
        String name = cobbleResourceService.randomName(male);
        String appearance = male ? "m" : "f";
        appearance += rand.nextInt(5); // hair color
        appearance += rand.nextInt(10); // hair style
        appearance += rand.nextInt(3); // eye color
        appearance += male ? rand.nextInt(5) : 0; // facial hair
        appearance += rand.nextInt(10); // shirt color
        appearance += rand.nextInt(3); // pant color

        return new CobbleNpc(id, name.split(" ")[0], name.split(" ")[1], appearance);
    }
}
