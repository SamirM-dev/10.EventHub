package com.example.eventhub.helper;

import java.time.LocalDateTime;

public interface HasEventTimeRange {
    LocalDateTime startTime();
    LocalDateTime endTime();
}
