package com.hirehub.candidature.mapper;

import com.hirehub.candidature.dtos.CandidatureCreatedDTO;
import com.hirehub.candidature.dtos.CandidatureResponseDTO;
import com.hirehub.candidature.dtos.HistoriqueStatusDTO;
import com.hirehub.candidature.entities.Candidature;
import com.hirehub.candidature.entities.HistoriqueStatus;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class HistoriqueStatusMapper {

    public HistoriqueStatusDTO toResponseDto(HistoriqueStatus entity) {
        if (entity == null) {
            return null;
        }

        HistoriqueStatusDTO dto = new HistoriqueStatusDTO();
        dto.setAncienStatut(entity.getAncienStatut());
        dto.setNouveauStatut(entity.getNouveauStatut());
        dto.setAuteur(entity.getAuteur());
        dto.setCommentaire(entity.getCommentaire());
        dto.setTimestamp(entity.getDateChangement().format(
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        return dto;
    }

    public List<HistoriqueStatusDTO> toResponseDtos(List<HistoriqueStatus> historiqueStatusList) {
        return historiqueStatusList.stream()
                .map(this::toResponseDto).toList();
    }

    public HistoriqueStatus toEntity(HistoriqueStatusDTO historiqueStatusDTO) {

        HistoriqueStatus historiqueStatus = new HistoriqueStatus();
        historiqueStatus.setAncienStatus(historiqueStatus.getAncienStatus());
        historiqueStatus.setId(historiqueStatus.getId());
        historiqueStatus.setNouveauStatus(historiqueStatus.getNouveauStatus());
        historiqueStatus.setCommentaire(historiqueStatus.getCommentaire());
        return historiqueStatus;
    }

}
