package com.bookstore.bookstore.application.port.in;

import com.bookstore.bookstore.application.command.CheckInReadingStreakCommand;
import com.bookstore.bookstore.application.result.ReadingStreakResult;
import java.util.UUID;

public interface IReadingStreakService {

    ReadingStreakResult getMyStreak(UUID userId);

    ReadingStreakResult checkIn(CheckInReadingStreakCommand command);
}
