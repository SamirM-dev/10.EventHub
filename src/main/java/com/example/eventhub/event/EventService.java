package com.example.eventhub.event;

import com.example.eventhub.helper.HelpForService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository repository;
    private final HelpForService helpForService;

    //availableSeats вычислить, но в бд не хранится
}
