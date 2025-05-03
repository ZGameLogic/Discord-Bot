package com.zgamelogic.services;

import com.zgamelogic.bot.services.CobbleResourceService;
import com.zgamelogic.data.database.cobbleData.CobbleBuildingType;
import com.zgamelogic.data.database.cobbleData.CobbleServiceException;
import com.zgamelogic.data.database.cobbleData.history.CobbleHistoryRepository;
import com.zgamelogic.data.database.cobbleData.npc.CobbleNpc;
import com.zgamelogic.data.database.cobbleData.npc.CobbleNpcRepository;
import com.zgamelogic.data.database.cobbleData.player.CobblePlayer;
import com.zgamelogic.data.database.cobbleData.player.CobblePlayerRepository;
import com.zgamelogic.data.database.cobbleData.production.CobbleProduction;
import com.zgamelogic.data.database.cobbleData.production.CobbleProductionRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.io.File;
import java.util.*;

import static com.zgamelogic.data.database.cobbleData.CobbleBuildingType.*;

@Service
@Slf4j
public class CobbleService {
    private final CobblePlayerRepository cobblePlayerRepository;
    private final CobbleHistoryRepository cobbleHistoryRepository;
    private final CobbleNpcRepository cobbleNpcRepository;
    private final CobbleProductionRepository cobbleProductionRepository;

    private final CobbleResourceService cobbleResourceService;

    private final File BASE_DIR;
    private final CobbleNpcIdRepository cobbleNpcIdRepository;

    public CobbleService(CobblePlayerRepository cobblePlayerRepository, CobbleHistoryRepository cobbleHistoryRepository, CobbleProductionRepository cobbleProductionRepository, CobbleResourceService cobbleResourceService, CobbleNpcRepository cobbleNpcRepository, CobbleNpcIdRepository cobbleNpcIdRepository) {
        this.cobblePlayerRepository = cobblePlayerRepository;
        this.cobbleHistoryRepository = cobbleHistoryRepository;
        this.cobbleProductionRepository = cobbleProductionRepository;
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
        cobblePlayerRepository.deleteById(232675572772372481L);
    }

    public CobblePlayer startCobblePlayer(long playerId) throws CobbleServiceException {
        if(cobblePlayerRepository.existsById(playerId)) throw new CobbleServiceException(("A cobble town already exists for this player"));
        CobblePlayer cobblePlayer = new CobblePlayer(playerId);
        UUID buildingUUID = UUID.randomUUID();
        cobblePlayer.addBuilding(TOWN_HALL, 1, "Town Hall", buildingUUID);
        cobblePlayer.addNpc(generateRandomCobbleNpc(cobblePlayer));
        cobblePlayer.addNpc(generateRandomCobbleNpc(cobblePlayer));
        cobblePlayer.getNpcs().get(0).setCobbleBuilding(cobblePlayer.getBuildings().get(0));
        return cobblePlayerRepository.save(cobblePlayer);
    }

    public List<CobbleNpc> getCobbleNpcs(long playerId) throws CobbleServiceException {
        if(!cobblePlayerRepository.existsById(playerId)) throw new CobbleServiceException("You must start the game first with the " + cobbleResourceService.cm("cobble start")  + " slash command");
        return cobbleNpcRepository.findAllByPlayer_PlayerId(playerId);
    }

    public CobbleNpc getCobbleNpc(long playerId, UUID npcId) throws CobbleServiceException {
        if(!cobblePlayerRepository.existsById(playerId)) throw new CobbleServiceException("You must start the game first with the " + cobbleResourceService.cm("cobble start")  + " slash command");
        Optional<CobbleNpc> npc = cobbleNpcRepository.findByPlayer_PlayerIdAndId(playerId, npcId);
        if(npc.isPresent()) return npc.get();
        throw new CobbleServiceException("Cobble npc not found");
    }

    public List<String> getCobbleBuildingList(){
        return Arrays.stream(values()).map(CobbleBuildingType::getFriendlyName).toList();
    }

    public List<CobbleProduction> getCobbleProductions(CobbleBuildingType buildingType){
        return cobbleProductionRepository.findAllById_Building(buildingType);
    }

    private CobbleNpc generateRandomCobbleNpc(CobblePlayer cobblePlayer) {
        Random rand = new Random();
        boolean male = rand.nextBoolean();
        String name = cobbleResourceService.randomName(male);
        String appearance = male ? "m" : "f";
        appearance += rand.nextInt(5); // skin color
        appearance += rand.nextInt(5); // hair color
        appearance += rand.nextInt(10); // hair style
        appearance += rand.nextInt(3); // eye color
        appearance += male ? rand.nextInt(5) : 0; // facial hair
        appearance += rand.nextInt(10); // shirt color
        appearance += rand.nextInt(3); // pant color
        return new CobbleNpc(cobblePlayer, name.split(" ")[0], name.split(" ")[1], appearance);
    }
}
