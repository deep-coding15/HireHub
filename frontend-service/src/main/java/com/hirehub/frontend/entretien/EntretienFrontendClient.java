package com.hirehub.frontend.entretien;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hirehub.common.dtos.ApiResponse;
import com.hirehub.frontend.auth.HirehubUserDetails;
import com.hirehub.frontend.auth.SessionAuthSupport;
import com.hirehub.frontend.offre.RecruiterContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

@Service
public class EntretienFrontendClient {

    private static final Logger log = LoggerFactory.getLogger(EntretienFrontendClient.class);

    private final RestTemplate restTemplate;
    private final String entretienBaseUrl;

    public EntretienFrontendClient(@Value("${hirehub.entretien-service-base-url}") String entretienBaseUrl) {
        this.entretienBaseUrl = entretienBaseUrl;
        // RestTemplate configuré avec JavaTimeModule pour sérialiser LocalDateTime en ISO 8601
        ObjectMapper mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        RestTemplate rt = new RestTemplate();
        rt.getMessageConverters().removeIf(c -> c instanceof MappingJackson2HttpMessageConverter);
        rt.getMessageConverters().add(0, new MappingJackson2HttpMessageConverter(mapper));
        this.restTemplate = rt;
    }

    public EntretienView create(EntretienCreateRequest request) {
        try {
            ResponseEntity<ApiResponse<EntretienView>> response = restTemplate.exchange(
                    entretienBaseUrl + "/entretiens",
                    HttpMethod.POST,
                    authEntity(request),
                    new ParameterizedTypeReference<ApiResponse<EntretienView>>() {}
            );
            ApiResponse<EntretienView> body = response.getBody();
            if (body != null && body.getData() != null) return body.getData();
            throw new EntretienException("Réponse invalide du service entretien");
        } catch (HttpStatusCodeException ex) {
            String msg = extractMessage(ex);
            log.warn("Création entretien : {} — {}", ex.getStatusCode(), msg);
            throw new EntretienException(msg);
        } catch (RestClientException ex) {
            throw new EntretienException("Service entretien indisponible", ex);
        }
    }

    private static String extractMessage(HttpStatusCodeException ex) {
        try {
            com.fasterxml.jackson.databind.JsonNode node =
                    new com.fasterxml.jackson.databind.ObjectMapper()
                            .readTree(ex.getResponseBodyAsString());
            com.fasterxml.jackson.databind.JsonNode msg = node.get("message");
            if (msg != null && !msg.isNull() && !msg.asText().isBlank()) {
                return msg.asText();
            }
        } catch (Exception ignored) {}
        return "Impossible de planifier l'entretien (erreur " + ex.getStatusCode().value() + ")";
    }

    public List<EntretienView> listByCandidature(String candidatureId) {
        return fetchList(entretienBaseUrl + "/entretiens/candidature/" + candidatureId);
    }

    public List<EntretienView> listForRecruiter() {
        HirehubUserDetails recruiter = RecruiterContext.requireRecruiter();
        return fetchList(entretienBaseUrl + "/entretiens/recruteur/" + recruiter.getId());
    }

    public List<EntretienView> listForCandidat(HirehubUserDetails candidat) {
        return fetchList(entretienBaseUrl + "/entretiens/candidat/" + candidat.getId());
    }

    private List<EntretienView> fetchList(String url) {
        try {
            ResponseEntity<ApiResponse<List<EntretienView>>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    authEntityNoBody(),
                    new ParameterizedTypeReference<ApiResponse<List<EntretienView>>>() {}
            );
            ApiResponse<List<EntretienView>> body = response.getBody();
            if (body != null && body.getData() != null) return body.getData();
            return Collections.emptyList();
        } catch (RestClientException ex) {
            log.warn("Entretiens API {} : {}", url, ex.getMessage());
            return Collections.emptyList();
        }
    }

    private HttpEntity<Object> authEntityNoBody() {
        return authEntity(null);
    }

    private <T> HttpEntity<T> authEntity(T body) {
        HttpHeaders headers = new HttpHeaders();
        SessionAuthSupport.accessToken().ifPresent(headers::setBearerAuth);
        if (body != null) headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(body, headers);
    }
}
