package tn.esprit.spring.diacarebackend.controller;

import tn.esprit.spring.diacarebackend.entities.GlycemieRecord;
import tn.esprit.spring.diacarebackend.services.IaDashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ia-dashboard")
@CrossOrigin(origins = "http://localhost:4200")
public class IaDashboardController {

    private final IaDashboardService iaService;

    public IaDashboardController(IaDashboardService iaService) {
        this.iaService = iaService;
    }

    @GetMapping("/dashboard/{patientId}")
    public ResponseEntity<Map<String, Object>> getDashboard(
            @PathVariable Long patientId) {
        return ResponseEntity.ok(iaService.getDashboard(patientId));
    }

    @PostMapping("/mesure/{patientId}")
    @Transactional
    public ResponseEntity<GlycemieRecord> addMesure(
            @PathVariable Long patientId,
            @RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(iaService.addMesure(
                patientId,
                Double.parseDouble(body.get("valeur").toString()),
                body.getOrDefault("moment", "AUTRE").toString(),
                body.getOrDefault("notes", "").toString()
        ));
    }

    @GetMapping("/historique/{patientId}")
    public ResponseEntity<List<GlycemieRecord>> getHistorique(
            @PathVariable Long patientId) {
        return ResponseEntity.ok(iaService.getHistorique(patientId));
    }
}
