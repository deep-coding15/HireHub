package com.hirehub.candidature.clients;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("FakeOffreServiceClient tests")
class FakeOffreServiceClientTest {

    private final FakeOffreServiceClient client = new FakeOffreServiceClient();

    @Test
    void shouldReturnPublishedOffer() {
        OffreDTO offre = client.getOffre("1001");

        assertNotNull(offre);
        assertEquals("1001", offre.getId());
        assertTrue(offre.isPublished());
        assertEquals("recruteur-1", offre.getRecruteurId());
    }

    @Test
    void shouldReturnFalseForUnknownOrUnpublishedOffer() {
        assertFalse(client.offreExists("9999"));
        assertFalse(client.offreExists("1003"));
    }

    @Test
    void shouldValidateRecruiterOwnership() {
        assertTrue(client.isRecruteurOwner("1001", "recruteur-1"));
        assertFalse(client.isRecruteurOwner("1001", "recruteur-2"));
        assertFalse(client.isRecruteurOwner("1003", "recruteur-1"));
    }
}

