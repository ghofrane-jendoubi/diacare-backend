package tn.esprit.spring.diacarebackend.controller;

import org.springframework.web.bind.annotation.*;
import tn.esprit.spring.diacarebackend.entities.FoodEntry;
import tn.esprit.spring.diacarebackend.services.FoodEntryService;

import java.util.List;

@RestController
@RequestMapping("/api/foods")
@CrossOrigin(origins = "*")
public class FoodEntryController {

    private final FoodEntryService service;

    public FoodEntryController(FoodEntryService service) {
        this.service = service;
    }


    // ✅ ajout
    @PostMapping
    public FoodEntry add(@RequestBody FoodEntryRequest request) {
        return service.addEntry(request.getText(), request.getPatientId());
    }

    @GetMapping
    public List<FoodEntry> getAll() {
        return service.getAll();
    }
}