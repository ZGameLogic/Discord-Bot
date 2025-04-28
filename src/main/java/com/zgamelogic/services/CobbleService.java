package com.zgamelogic.services;

import com.zgamelogic.data.database.cobbleData.player.CobblePlayerRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.io.File;

@Service
@Slf4j
public class CobbleService {
    private final CobblePlayerRepository cobblePlayerRepository;

    private final File BASE_DIR;

    public CobbleService(CobblePlayerRepository cobblePlayerRepository) {
        this.cobblePlayerRepository = cobblePlayerRepository;
        File file = new File("/cobble"); // check if we are in cluster
        if(!file.exists()) file = new File("cobble"); // change to local
        BASE_DIR = file;
        log.info("Cobble service started");
    }

    @PostConstruct
    public void init() {
        cobblePlayerRepository.getReferenceById(15L).getBuildings().forEach(building -> {
            System.out.println(building.getProduction().getProduction());
        });
    }
}
