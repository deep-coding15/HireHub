package com.hirehub.frontend.candidature;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Service
public class ApplicationUploadService {

    private final Path uploadRoot;

    public ApplicationUploadService(@Value("${hirehub.upload-dir:./data/uploads}") String uploadDir) {
        this.uploadRoot = Path.of(uploadDir).toAbsolutePath().normalize();
    }

    public String storeCv(UUID candidatId, String offreId, MultipartFile cv) throws IOException {
        if (cv == null || cv.isEmpty()) {
            throw new IllegalArgumentException("Le CV est obligatoire.");
        }
        String original = cv.getOriginalFilename();
        String ext = ".pdf";
        if (StringUtils.hasText(original) && original.contains(".")) {
            ext = original.substring(original.lastIndexOf('.')).toLowerCase();
        }
        Path dir = uploadRoot.resolve(candidatId.toString()).resolve(offreId);
        Files.createDirectories(dir);
        String fileName = "cv" + ext;
        Path target = dir.resolve(fileName);
        cv.transferTo(target);
        return candidatId + "/" + offreId + "/" + fileName;
    }

    public String storeLettre(UUID candidatId, String offreId, String lettreText) throws IOException {
        Path dir = uploadRoot.resolve(candidatId.toString()).resolve(offreId);
        Files.createDirectories(dir);
        Path target = dir.resolve("lettre.txt");
        String content = StringUtils.hasText(lettreText) ? lettreText : "";
        Files.writeString(target, content, StandardCharsets.UTF_8);
        return candidatId + "/" + offreId + "/lettre.txt";
    }

    public java.util.Optional<Path> resolveDownloadPath(UUID candidatId, String offreId, String fileType) throws IOException {
        Path dir = uploadRoot.resolve(candidatId.toString()).resolve(offreId);
        if (!Files.isDirectory(dir)) {
            return java.util.Optional.empty();
        }
        if ("lettre".equalsIgnoreCase(fileType)) {
            Path lettre = dir.resolve("lettre.txt");
            return Files.exists(lettre) ? java.util.Optional.of(lettre) : java.util.Optional.empty();
        }
        try (var stream = Files.list(dir)) {
            return stream
                    .filter(p -> {
                        String name = p.getFileName().toString().toLowerCase();
                        return name.startsWith("cv") && (name.endsWith(".pdf") || name.endsWith(".doc") || name.endsWith(".docx"));
                    })
                    .findFirst();
        }
    }
}
