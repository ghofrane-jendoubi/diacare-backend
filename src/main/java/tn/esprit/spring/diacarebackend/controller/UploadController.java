package tn.esprit.spring.diacarebackend.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
@CrossOrigin(origins = "http://localhost:4200")
public class UploadController {

    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    @PostMapping("/image")
    public ResponseEntity<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            System.out.println("=== UPLOAD IMAGE ===");
            System.out.println("Nom fichier: " + file.getOriginalFilename());
            System.out.println("Taille: " + file.getSize());
            System.out.println("Type: " + file.getContentType());
            System.out.println("Upload dir: " + uploadDir);

            if (file.isEmpty()) {
                return ResponseEntity.status(400).body(Map.of("error", "Fichier vide"));
            }

            // Créer le dossier s'il n'existe pas
            String uploadPath = uploadDir + "/images/";
            File directory = new File(uploadPath);
            if (!directory.exists()) {
                System.out.println("Création du dossier: " + uploadPath);
                boolean created = directory.mkdirs();
                System.out.println("Dossier créé: " + created);
            }

            // Générer un nom de fichier unique
            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            String filePath = uploadPath + fileName;

            // Sauvegarder le fichier
            System.out.println("Sauvegarde vers: " + filePath);
            file.transferTo(new File(filePath));

            // URL accessible depuis le frontend
            String fileUrl = "/uploads/images/" + fileName;

            Map<String, String> response = new HashMap<>();
            response.put("imageUrl", fileUrl);

            System.out.println("Upload réussi: " + fileUrl);
            return ResponseEntity.ok(response);

        } catch (IOException e) {
            System.err.println("IOException: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            System.err.println("Exception: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/audio")
    public ResponseEntity<Map<String, Object>> uploadAudio(@RequestParam("file") MultipartFile file) {
        try {
            System.out.println("=== UPLOAD AUDIO ===");
            System.out.println("Nom fichier: " + file.getOriginalFilename());
            System.out.println("Taille: " + file.getSize());

            String uploadPath = uploadDir + "/audio/";
            File directory = new File(uploadPath);
            if (!directory.exists()) {
                System.out.println("Création du dossier: " + uploadPath);
                boolean created = directory.mkdirs();
                System.out.println("Dossier créé: " + created);
            }

            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            String filePath = uploadPath + fileName;

            file.transferTo(new File(filePath));

            String fileUrl = "/uploads/audio/" + fileName;

            Map<String, Object> response = new HashMap<>();
            response.put("audioUrl", fileUrl);

            System.out.println("Upload audio réussi: " + fileUrl);
            return ResponseEntity.ok(response);

        } catch (IOException e) {
            System.err.println("IOException: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}