package tn.esprit.spring.diacarebackend.dto;



import lombok.Data;

@Data
public class TopPostDTO {
    private Long id;
    private String title;
    private int likeCount;
    private int commentCount;
}
