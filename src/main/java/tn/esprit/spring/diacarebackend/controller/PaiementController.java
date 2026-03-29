package tn.esprit.spring.diacarebackend.controller;

import com.stripe.model.PaymentIntent;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.esprit.spring.diacarebackend.services.PaiementService;

import java.util.Map;

@RestController
@RequestMapping("/api/paiement")
@RequiredArgsConstructor
public class PaiementController {

    private final PaiementService paiementService;

    @PostMapping("/create")
    public Map<String, String> create(@RequestParam Long amount) throws Exception {

        PaymentIntent intent = paiementService.createPaymentIntent(amount);

        return Map.of("clientSecret", intent.getClientSecret());
    }

}