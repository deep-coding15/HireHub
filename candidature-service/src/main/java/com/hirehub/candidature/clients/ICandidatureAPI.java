package com.hirehub.candidature.clients;

import com.hirehub.candidature.dtos.CandidatureCreatedDTO;
import com.hirehub.candidature.dtos.CandidatureResponseDTO;
import com.hirehub.candidature.dtos.HistoriqueStatusDTO;
import com.hirehub.candidature.entities.Candidature;
import com.hirehub.candidature.entities.HistoriqueStatus;
import com.hirehub.common.dtos.ApiResponse;
import com.hirehub.common.dtos.candidatures.CandidatureDTO;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.core.io.Resource;
import org.springframework.core.io.FileSystemResource;

/**
 * Interface API pour les candidatures
 * Définit les contrats pour les endpoints REST
 */
@Tag(name = "Candidatures", description = "Gestion des candidatures")
public interface ICandidatureAPI {

    /**
     * Crée une nouvelle candidature
     */
    ResponseEntity<ApiResponse<CandidatureResponseDTO>> create(
            @Valid @RequestBody CandidatureCreatedDTO candidatureDTO);

    /**
     * Récupère les candidatures du candidat
     */
    ResponseEntity<ApiResponse<List<CandidatureResponseDTO>>> myCandidatures();

    /**
     * Récupère les candidatures pour une offre
     */
    ResponseEntity<ApiResponse<List<CandidatureResponseDTO>>> getCandidaturesByOffreForRecruteur(@PathVariable String offreId);

    /**
     * Récupère une candidature par ID
     */
    ResponseEntity<ApiResponse<CandidatureResponseDTO>> get(@PathVariable String id);

    /**
     * Met à jour le statut
     */
    ResponseEntity<ApiResponse<CandidatureResponseDTO>> updateStatus(@PathVariable String candidatureId,
                                                                 @RequestParam("status") String newStatus);

    /**
     * Met à jour les fichiers
     */
    ResponseEntity<ApiResponse<CandidatureResponseDTO>> updateFiles(@PathVariable String id,
                                                                @RequestParam(value = "cvPath", required = false) String cvPath,
                                                                @RequestParam(value = "lettreMotivationPath", required = false) String lettreMotivationPath);

    /**
     * Récupère l'historique
     */
    ResponseEntity<ApiResponse<List<HistoriqueStatusDTO>>> getHistorique(@PathVariable String candidatureId) throws Exception;

    /**
     * Upload des fichiers
     */
    ApiResponse<Candidature> uploadFiles(String candidatureId, String CV_Path, String lettreMotivationPath);

    /**
     * Supprime une candidature
     */
    ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id);

    ResponseEntity<Resource> downloadFile(
            @PathVariable String id,
            @RequestParam(value = "type", defaultValue = "cv") String fileType);

}
