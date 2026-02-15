package com.atminterface.atm.controller;

import com.atminterface.atm.service.AtmService;
import com.atminterface.common.security.AuthInterceptor;
import com.atminterface.common.security.SessionPrincipal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AtmController.class)
class AtmControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AtmService atmService;

    @MockBean
    private AuthInterceptor authInterceptor;

    @Test
    void changePinShouldReturnSuccess() throws Exception {
        doNothing().when(atmService).changePin(any(SessionPrincipal.class), any());

        mockMvc.perform(post("/api/atm/change-pin")
                        .requestAttr(AuthInterceptor.PRINCIPAL_ATTR, new SessionPrincipal(UUID.randomUUID(), "123"))
                        .contentType("application/json")
                        .content("{\"oldPin\":\"1234\",\"newPin\":\"5678\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
