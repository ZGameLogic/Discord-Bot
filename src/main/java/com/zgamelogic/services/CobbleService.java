package com.zgamelogic.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
@Slf4j
public class CobbleService {
    private final File BASE_DIR;

    public CobbleService() {
        File file = new File("/cobble"); // check if we are in cluster
        if(!file.exists()) file = new File("cobble"); // change to local
        BASE_DIR = file;
        log.info("Cobble service started");
    }
}
