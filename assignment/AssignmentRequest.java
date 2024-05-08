package com.mchis.assignment;

import java.time.LocalDateTime;

public record AssignmentRequest(
        String title,
        String name,
        LocalDateTime startTime,
        LocalDateTime endTime
) {
}
