package tn.esprit.spring.diacarebackend.entities;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
public class Delivery {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long orderId;                  // lien vers la commande
    private String trackingCode;           // ex: "DEL-20250413-001"
    private String status;                 // CONFIRMED, PREPARING, SHIPPED, OUT_FOR_DELIVERY, DELIVERED, CANCELLED
    private LocalDateTime estimatedArrival;
    private String carrier;                // "FastExpress"
    private String vehicle;                // "Moto"
    private LocalDateTime updatedAt;
    private String notes;                  // "Médical – livraison prioritaire"
}