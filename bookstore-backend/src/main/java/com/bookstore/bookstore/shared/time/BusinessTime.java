package com.bookstore.bookstore.shared.time;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BusinessTime {

    private final Clock clock;

    public Instant nowInstant() {
        return Instant.now(clock);
    }

    public LocalDate todayLocalDate() {
        return LocalDate.now(clock);
    }

    public Instant startOfDayInstant(LocalDate date) {
        return date.atStartOfDay(clock.getZone()).toInstant();
    }

    public ZonedDateTime toZonedDateTime(Instant instant) {
        return instant.atZone(clock.getZone());
    }
}
