package com.zgamelogic.services;

import com.modernmt.text.profanity.ProfanityFilter;
import org.springframework.stereotype.Service;

@Service
public class BadNameService {
    private final ProfanityFilter profanityFilter;

    public BadNameService() {
        profanityFilter = new ProfanityFilter();
    }

    public boolean isNotOkay(String name){
        return profanityFilter.test("en", name);
    }
}
