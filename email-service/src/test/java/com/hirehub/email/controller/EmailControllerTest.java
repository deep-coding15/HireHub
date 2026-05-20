package com.hirehub.email.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hirehub.common.dtos.candidatures.CandidatureDTO;
import com.hirehub.common.dtos.candidatures.CandidatureStatutChangedDTO;
import com.hirehub.common.dtos.entretiens.EntretienPlanifiedDTO;
import com.hirehub.email.email.interfaces.EmailBusinessService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = EmailController.class)
public class EmailControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmailBusinessService emailBusinessService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void sendCandidatureConfirmation_valid_shouldReturnOk() throws Exception {
        CandidatureDTO dto = new CandidatureDTO();
        dto.setId("c1");
        dto.setCandidatId("u1");
        dto.setOffreId("o1");

        Mockito.doNothing().when(emailBusinessService).sendCandidatureConfirmation(Mockito.any());

        mockMvc.perform(post("/notification-service/notifications/candidature-confirmation")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    void sendCandidatureConfirmation_missingFields_shouldReturnBadRequest() throws Exception {
        CandidatureDTO dto = new CandidatureDTO();
        dto.setId("c2");

        mockMvc.perform(post("/notification-service/notifications/candidature-confirmation")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

}

