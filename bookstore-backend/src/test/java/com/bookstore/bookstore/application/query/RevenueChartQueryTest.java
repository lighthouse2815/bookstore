package com.bookstore.bookstore.application.query;

import com.bookstore.bookstore.application.enums.RevenueGroupBy;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RevenueChartQueryTest {

    @Test
    void constructor_defaultsGroupByToDay() {
        RevenueChartQuery query = new RevenueChartQuery(null, null, null);

        assertEquals(RevenueGroupBy.DAY, query.groupBy());
    }

    @Test
    void constructor_rejectsIncompleteDateRange() {
        LocalDate date = LocalDate.of(2026, 7, 1);

        assertThrows(
                IllegalArgumentException.class,
                () -> new RevenueChartQuery(date, null, RevenueGroupBy.DAY)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> new RevenueChartQuery(null, date, RevenueGroupBy.DAY)
        );
    }

    @Test
    void constructor_rejectsReversedDateRange() {
        LocalDate from = LocalDate.of(2026, 7, 31);
        LocalDate to = LocalDate.of(2026, 7, 1);

        assertThrows(
                IllegalArgumentException.class,
                () -> new RevenueChartQuery(from, to, RevenueGroupBy.DAY)
        );
    }
}
