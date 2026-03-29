package tn.esprit.spring.diacarebackend.entities;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
@Data
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 5000)
    private String content; // Contenu texte

    // ==== EXPÉDITEUR / DESTINATAIRE ====
    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    // ==== TIMESTAMPS ====
    private LocalDateTime sentAt;    // Date d'envoi
    private LocalDateTime deliveredAt; // Date de réception
    private LocalDateTime seenAt;     // Date de lecture

    // ==== STATUTS ====
    private boolean delivered = false; // Message reçu
    private boolean seen = false;      // Message lu

    // ==== FICHIERS ====
    private String imageUrl;      // Photo de la langue
    private String audioUrl;      // Message vocal
    private Integer audioDuration; // Durée en secondes
    private String documentUrl;


}