package com.bookstore.bookstore.presentation.response;

import java.time.LocalDate;

public record ReadingStreakResponse(
        int currentStreak,
        int longestStreak,
        boolean checkedInToday,
        LocalDate lastActivityDate
) {
}
