package com.hirehub.frontend.viewmodels;

import com.hirehub.frontend.clients.CandidatureDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * ViewModel pour afficher les candidatures dans le pipeline recruteur
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PipelineViewModel {
    private String candidatureId;
    private String candidatId;
    private String candidatEmail;
    private String status;
    private LocalDateTime dateSoumission;
    private String cvPath;
    private String statusLabel;
    private String statusBadgeClass;

    public static PipelineViewModel fromDTO(CandidatureDTO dto) {
        PipelineViewModel vm = new PipelineViewModel();
        vm.setCandidatureId(dto.getId());
        vm.setCandidatId(dto.getCandidatId());
        vm.setStatus(dto.getStatus());
        vm.setDateSoumission(dto.getDateSoumission());
        vm.setCvPath(dto.getCvPath());

        if (dto.getStatus() != null) {
            switch (dto.getStatus()) {
                case "SOUMISE":
                    vm.setStatusLabel("Soumise");
                    vm.setStatusBadgeClass("badge bg-info");
                    break;
                case "EN_COURS":
                    vm.setStatusLabel("En cours");
                    vm.setStatusBadgeClass("badge bg-warning");
                    break;
                case "ENTRETIEN":
                    vm.setStatusLabel("Entretien");
                    vm.setStatusBadgeClass("badge bg-primary");
                    break;
                case "ACCEPTEE":
                    vm.setStatusLabel("Acceptée");
                    vm.setStatusBadgeClass("badge bg-success");
                    break;
                case "REFUSEE":
                    vm.setStatusLabel("Refusée");
                    vm.setStatusBadgeClass("badge bg-danger");
                    break;
                default:
                    vm.setStatusLabel(dto.getStatus());
                    vm.setStatusBadgeClass("badge bg-secondary");
            }
        }

        return vm;
    }

    public String getDateSoumissionFormatted() {
        if (dateSoumission == null) return "-";
        return dateSoumission.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    public boolean hasCv() {
        return cvPath != null && !cvPath.isEmpty();
    }
}

