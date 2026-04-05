package tn.esprit.spring.diacarebackend.DTOs;

import lombok.Data;

@Data
public class CategoryCountDTO {
    private String category;
    private long count;
}