package com.zgamelogic.services;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

@Service
public class CobbleService {
    private final File BASE_DIR;

    public CobbleService() {
        BASE_DIR = new File("cobble");
    }

    @PostConstruct
    public void init() throws IOException {
        File out = new File(BASE_DIR, "cobble.txt");
        System.out.println(out.exists());
        if(!out.exists()){
            out.getParentFile().mkdirs();
            out.createNewFile();
        }
        PrintWriter writer = new PrintWriter(out);
        writer.println("Ben we made it");
        writer.flush();
        writer.close();
        System.out.println("Wrote cobble.txt");
        System.out.println(out.exists());
    }
}
