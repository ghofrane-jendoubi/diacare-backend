package tn.esprit.spring.diacarebackend.DTOs;



import lombok.Data;

@Data
public class TopPostDTO {
    private Long id;
    private String title;
    private int likeCount;
    private int commentCount;
}
