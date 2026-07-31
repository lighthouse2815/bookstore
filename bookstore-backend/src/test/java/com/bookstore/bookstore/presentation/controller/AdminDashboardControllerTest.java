package com.bookstore.bookstore.presentation.controller;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bookstore.bookstore.application.enums.RevenueGroupBy;
import com.bookstore.bookstore.application.port.in.IAdminDashboardService;
import com.bookstore.bookstore.application.query.RevenueChartQuery;
import com.bookstore.bookstore.application.result.RevenueChartResult;
import com.bookstore.bookstore.presentation.mapper.AdminDashboardWebMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class AdminDashboardControllerTest {

    @Mock
    private IAdminDashboardService adminDashboardService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(
                new AdminDashboardController(
                        adminDashboardService,
                        new AdminDashboardWebMapper()
                )
        ).build();
    }

    @Test
    void getRevenue_bindsAndNormalizesQueryRequest() throws Exception {
        LocalDate from = LocalDate.of(2026, 7, 1);
        LocalDate to = LocalDate.of(2026, 7, 31);
        RevenueChartQuery query = new RevenueChartQuery(from, to, RevenueGroupBy.MONTH);
        when(adminDashboardService.getRevenue(query))
                .thenReturn(List.of(new RevenueChartResult(
                        "07/2026",
                        new BigDecimal("1250000"),
                        8
                )));

        mockMvc.perform(get("/api/admin/dashboard/revenue")
                        .param("from", "2026-07-01")
                        .param("to", "2026-07-31")
                        .param("groupBy", " month "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].label").value("07/2026"))
                .andExpect(jsonPath("$.data[0].revenue").value(1250000))
                .andExpect(jsonPath("$.data[0].orders").value(8));

        verify(adminDashboardService).getRevenue(query);
    }

    @Test
    void getRevenue_whenDateRangeIsInvalid_rejectsBeforeService() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard/revenue")
                        .param("from", "2026-07-31")
                        .param("to", "2026-07-01")
                        .param("groupBy", "DAY"))
                .andExpect(status().isBadRequest());

        verify(adminDashboardService, never()).getRevenue(org.mockito.ArgumentMatchers.any());
    }
}
