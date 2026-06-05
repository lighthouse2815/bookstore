package com.bookstore.bookstore.infrastructure.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bookstore.bookstore.presentation.controller.TestController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TestController.class)
@Import(SecurityConfig.class)
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void apiTestEndpoint_isAccessibleWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/test"))
                .andExpect(status().isOk())
                .andExpect(content().string("Bookstore API is running"));
    }
}
