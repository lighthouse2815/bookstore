package com.bookstore.bookstore.application.service;

import com.bookstore.bookstore.application.command.CheckInReadingStreakCommand;
import com.bookstore.bookstore.application.command.UpsertReadingJournalEntryCommand;
import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import com.bookstore.bookstore.application.port.in.IReadingJournalService;
import com.bookstore.bookstore.application.port.in.IReadingStreakService;
import com.bookstore.bookstore.application.port.out.IReadingJournalRepository;
import com.bookstore.bookstore.application.result.ReadingStreakResult;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReadingStreakService implements IReadingStreakService {

    private static final ZoneId STREAK_ZONE = ZoneId.systemDefault();

    private final IReadingJournalRepository readingJournalRepository;
    private final IReadingJournalService readingJournalService;

    @Override
    @Transactional(readOnly = true)
    public ReadingStreakResult getMyStreak(UUID userId) {
        if (userId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "userId");
        }

        LocalDate today = LocalDate.now(STREAK_ZONE);
        List<LocalDate> activityDates = new ArrayList<>(
                readingJournalRepository.findDistinctEntryDatesByUserIdActive(userId)
        );
        activityDates.sort(Comparator.naturalOrder());
        if (activityDates.isEmpty()) {
            return new ReadingStreakResult(0, 0, false, null);
        }

        LocalDate lastActivityDate = activityDates.get(activityDates.size() - 1);
        boolean checkedInToday = lastActivityDate.equals(today);

        return new ReadingStreakResult(
                calculateCurrentStreak(activityDates, today),
                calculateLongestStreak(activityDates),
                checkedInToday,
                lastActivityDate
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReadingStreakResult checkIn(CheckInReadingStreakCommand command) {
        if (command == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "command");
        }
        if (command.userId() == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "userId");
        }
        if (command.bookId() == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "bookId");
        }

        readingJournalService.upsert(new UpsertReadingJournalEntryCommand(
                command.userId(),
                command.bookId(),
                LocalDate.now(STREAK_ZONE),
                command.note(),
                command.currentPage(),
                command.progressPercent()
        ));
        return getMyStreak(command.userId());
    }

    private int calculateCurrentStreak(List<LocalDate> activityDates, LocalDate today) {
        if (activityDates.isEmpty()) {
            return 0;
        }

        LocalDate lastActivityDate = activityDates.get(activityDates.size() - 1);
        if (lastActivityDate.isBefore(today.minusDays(1))) {
            return 0;
        }

        int streak = 1;
        LocalDate cursor = lastActivityDate;
        for (int index = activityDates.size() - 2; index >= 0; index--) {
            LocalDate date = activityDates.get(index);
            if (date.equals(cursor.minusDays(1))) {
                streak++;
                cursor = date;
                continue;
            }

            if (!date.equals(cursor)) {
                break;
            }
        }

        return streak;
    }

    private int calculateLongestStreak(List<LocalDate> activityDates) {
        if (activityDates.isEmpty()) {
            return 0;
        }

        int longest = 1;
        int current = 1;
        for (int index = 1; index < activityDates.size(); index++) {
            LocalDate currentDate = activityDates.get(index);
            LocalDate previousDate = activityDates.get(index - 1);

            if (currentDate.equals(previousDate.plusDays(1))) {
                current++;
            } else if (!currentDate.equals(previousDate)) {
                current = 1;
            }

            longest = Math.max(longest, current);
        }

        return longest;
    }
}
