package com.bookstore.bookstore.application.result;

import java.time.LocalDate;

public record ReadingStreakResult(
        int currentStreak,
        int longestStreak,
        boolean checkedInToday,
        LocalDate lastActivityDate
) {
}
