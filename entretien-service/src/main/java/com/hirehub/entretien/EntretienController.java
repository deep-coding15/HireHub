package com.hirehub.entretien;

import com.hirehub.common.dtos.ApiResponse;
import com.hirehub.entretien.dtos.CreateEntretienRequest;
import com.hirehub.entretien.dtos.EntretienResponse;
import com.hirehub.entretien.services.EntretienService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/entretiens")
public class EntretienController {

    private final EntretienService entretienService;

    public EntretienController(EntretienService entretienService) {
        this.entretienService = entretienService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<EntretienResponse>> create(
            @RequestBody CreateEntretienRequest request) {
        try {
            EntretienResponse response = EntretienResponse.from(entretienService.create(request));
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.ok("Entretien planifie", response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/candidature/{candidatureId}")
    public ResponseEntity<ApiResponse<List<EntretienResponse>>> listByCandidature(
            @PathVariable String candidatureId) {
        List<EntretienResponse> list = entretienService.listByCandidature(candidatureId)
                .stream().map(EntretienResponse::from).toList();
        return ResponseEntity.ok(ApiResponse.ok("Entretiens recuperes", list));
    }

    @GetMapping("/recruteur/{recruteurId}")
    public ResponseEntity<ApiResponse<List<EntretienResponse>>> listByRecruteur(
            @PathVariable String recruteurId) {
        List<EntretienResponse> list = entretienService.listByRecruteur(recruteurId)
                .stream().map(EntretienResponse::from).toList();
        return ResponseEntity.ok(ApiResponse.ok("Entretiens recuperes", list));
    }

    @GetMapping("/candidat/{candidatId}")
    public ResponseEntity<ApiResponse<List<EntretienResponse>>> listByCandidat(
            @PathVariable String candidatId) {
        List<EntretienResponse> list = entretienService.listByCandidat(candidatId)
                .stream().map(EntretienResponse::from).toList();
        return ResponseEntity.ok(ApiResponse.ok("Entretiens recuperes", list));
    }

    @DeleteMapping("/{entretienId}")
    public ResponseEntity<ApiResponse<EntretienResponse>> cancel(
            @PathVariable String entretienId,
            @RequestParam(required = false) String recruteurId,
            @RequestHeader(value = "X-Recruteur-Id", required = false) String recruteurIdHeader) {
        String effectiveRecruteurId = recruteurId != null ? recruteurId : recruteurIdHeader;
        try {
            EntretienResponse response = EntretienResponse.from(
                    entretienService.cancel(entretienId, effectiveRecruteurId));
            return ResponseEntity.ok(ApiResponse.ok("Entretien annule", response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error(e.getMessage()));
        }
    }
}