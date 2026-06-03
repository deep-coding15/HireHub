package com.hirehub.frontend.clients;

import com.hirehub.common.dtos.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(
        name = "candidature-service",
        fallback = CandidatureServiceClientFallback.class
)
public interface CandidatureServiceClient {

    @GetMapping("/candidatures/moi")
    ApiResponse<List<CandidatureDTO>> getMyCandidatures();

    @GetMapping("/candidatures/{id}")
    ApiResponse<CandidatureDTO> getCandidature(@PathVariable("id") String id);

    @PostMapping("/candidatures")
    ApiResponse<CandidatureDTO> createCandidature(@RequestBody CandidatureDTO dto);

    @PutMapping("/candidatures/{id}/status")
    ApiResponse<CandidatureDTO> updateStatus(
        @PathVariable("id") String id,
        @RequestParam("status") String status
    );

    @DeleteMapping("/candidatures/{id}")
    ApiResponse<Void> deleteCandidature(@PathVariable("id") String id);

    @GetMapping("/candidatures/{id}/historique")
    ApiResponse<List<HistoriqueStatutDTO>> getHistorique(@PathVariable("id") String id);

    @GetMapping("/candidatures/offre/{offreId}")
    ApiResponse<List<CandidatureDTO>> getCandidaturesByOffre(@PathVariable("offreId") String offreId);

    /** URL corrigée : /candidatures/file/{id}/download */
    @GetMapping("/candidatures/file/{id}/download")
    ResponseEntity<byte[]> downloadFile(
        @PathVariable("id") String id,
        @RequestParam(value = "type", defaultValue = "cv") String fileType
    );

    @PatchMapping("/candidatures/{id}")
    ApiResponse<CandidatureDTO> updateFiles(
        @PathVariable("id") String id,
        @RequestParam(value = "cvPath", required = false) String cvPath,
        @RequestParam(value = "lettreMotivationPath", required = false) String lettreMotivationPath
    );
}
