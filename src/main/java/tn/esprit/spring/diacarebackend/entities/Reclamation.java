package tn.esprit.spring.diacarebackend.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reclamations")
public class Reclamation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReclamationCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReclamationStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReclamationPriority priority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole createdByRole;

    @Column(nullable = false)
    private Long createdById;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole targetRole;

    @Enumerated(EnumType.STRING)
    private UserRole handledByRole;

    private Long handledById;

    @Column(columnDefinition = "TEXT")
    private String adminResponse;

    @Column(columnDefinition = "TEXT")
    private String internalNote;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime resolvedAt;

    public Reclamation() {}

    @PrePersist
    public void prePersist() {
        if (status == null) status = ReclamationStatus.OPEN;
        if (priority == null) priority = ReclamationPriority.MEDIUM;
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
        if (status == ReclamationStatus.RESOLVED && resolvedAt == null) {
            resolvedAt = LocalDateTime.now();
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public ReclamationCategory getCategory() { return category; }
    public void setCategory(ReclamationCategory category) { this.category = category; }
    public ReclamationStatus getStatus() { return status; }
    public void setStatus(ReclamationStatus status) { this.status = status; }
    public ReclamationPriority getPriority() { return priority; }
    public void setPriority(ReclamationPriority priority) { this.priority = priority; }
    public UserRole getCreatedByRole() { return createdByRole; }
    public void setCreatedByRole(UserRole createdByRole) { this.createdByRole = createdByRole; }
    public Long getCreatedById() { return createdById; }
    public void setCreatedById(Long createdById) { this.createdById = createdById; }
    public UserRole getTargetRole() { return targetRole; }
    public void setTargetRole(UserRole targetRole) { this.targetRole = targetRole; }
    public UserRole getHandledByRole() { return handledByRole; }
    public void setHandledByRole(UserRole handledByRole) { this.handledByRole = handledByRole; }
    public Long getHandledById() { return handledById; }
    public void setHandledById(Long handledById) { this.handledById = handledById; }
    public String getAdminResponse() { return adminResponse; }
    public void setAdminResponse(String adminResponse) { this.adminResponse = adminResponse; }
    public String getInternalNote() { return internalNote; }
    public void setInternalNote(String internalNote) { this.internalNote = internalNote; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }
}
