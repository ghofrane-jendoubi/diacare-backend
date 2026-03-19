package tn.esprit.spring.diacarebackend.controller;

import org.springframework.web.bind.annotation.*;
import tn.esprit.spring.diacarebackend.entities.FoodEntry;
import tn.esprit.spring.diacarebackend.services.FoodEntryService;

import java.util.List;

@RestController
@RequestMapping("/api/food")
@CrossOrigin(origins = "http://localhost:4200")
public class FoodEntryController {

    private final FoodEntryService service;

    public FoodEntryController(FoodEntryService service) {
        this.service = service;
    }

    // ✅ ajout
    @PostMapping
    public FoodEntry add(@RequestParam String text,
                         @RequestParam Long patientId) {
        return service.addEntry(text, patientId);
    }

    // ✅ afficher
    @GetMapping
    public List<FoodEntry> getAll() {
        return service.getAll();
    }
}