package com.hirehub.frontend.entretien;

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
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

@Service
public class EntretienFrontendClient {

    private static final Logger log = LoggerFactory.getLogger(EntretienFrontendClient.class);

    private final RestTemplate restTemplate = new RestTemplate();
    private final String entretienBaseUrl;

    public EntretienFrontendClient(@Value("${hirehub.entretien-service-base-url}") String entretienBaseUrl) {
        this.entretienBaseUrl = entretienBaseUrl;
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
            log.warn("Création entretien : {} {}", ex.getStatusCode(), ex.getResponseBodyAsString());
            throw new EntretienException("Impossible de planifier l'entretien : " + ex.getStatusCode());
        } catch (RestClientException ex) {
            throw new EntretienException("Service entretien indisponible", ex);
        }
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
