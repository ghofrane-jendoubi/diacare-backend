package tn.esprit.spring.diacarebackend.dto;

import lombok.Data;

@Data
public class TopContributorDTO {
    private Long patientId;
    private String patientName;
    private Long postCount;
    private Long totalLikes;
    private Long totalComments;
    
    // Constructeur par défaut requis par JPA
    public TopContributorDTO() {}
    
    // Constructeur avec tous les paramètres requis
    public TopContributorDTO(Long patientId, String patientName, Long postCount, Long totalLikes, Long totalComments) {
        this.patientId = patientId;
        this.patientName = patientName;
        this.postCount = postCount;
        this.totalLikes = totalLikes;
        this.totalComments = totalComments;
    }
}
