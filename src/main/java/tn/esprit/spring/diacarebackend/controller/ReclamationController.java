package tn.esprit.spring.diacarebackend.controller;

import org.springframework.web.bind.annotation.*;
import tn.esprit.spring.diacarebackend.entities.*;
import tn.esprit.spring.diacarebackend.services.ReclamationService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reclamations")
@CrossOrigin(origins = "http://localhost:4200")
public class ReclamationController {

    private final ReclamationService reclamationService;

    public ReclamationController(ReclamationService reclamationService) {
        this.reclamationService = reclamationService;
    }

    @PostMapping
    public Reclamation create(@RequestBody Reclamation reclamation) {
        return reclamationService.create(reclamation);
    }

    @PutMapping("/{id}")
    public Reclamation update(@PathVariable Long id, @RequestBody Reclamation reclamation) {
        reclamation.setId(id);
        return reclamationService.create(reclamation);
    }

    @GetMapping
    public List<Reclamation> getAll() {
        return reclamationService.getAll();
    }

    @GetMapping("/{id}")
    public Reclamation getById(@PathVariable Long id) {
        return reclamationService.getById(id);
    }

    @GetMapping("/mine")
    public List<Reclamation> getMine(@RequestParam Long userId, @RequestParam UserRole role) {
        return reclamationService.getMine(userId, role);
    }

    @GetMapping("/target/{role}")
    public List<Reclamation> getByTargetRole(@PathVariable UserRole role) {
        return reclamationService.getByTargetRole(role);
    }

    @PutMapping("/{id}/status")
    public Reclamation updateStatus(
            @PathVariable Long id,
            @RequestParam ReclamationStatus status,
            @RequestParam(required = false) Long handledById,
            @RequestParam(required = false) UserRole handledByRole) {
        return reclamationService.updateStatus(id, status, handledById, handledByRole);
    }

    @PutMapping("/{id}/response")
    public Reclamation respond(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            @RequestParam(required = false) Long handledById,
            @RequestParam(required = false) UserRole handledByRole) {
        return reclamationService.respond(
                id,
                body.getOrDefault("response", ""),
                body.getOrDefault("internalNote", ""),
                handledById,
                handledByRole
        );
    }

    @GetMapping("/stats")
    public Map<String, Long> getStats() {
        return reclamationService.getStats();
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        reclamationService.delete(id);
    }
}