package tn.esprit.spring.diacarebackend.dto;

import tn.esprit.spring.diacarebackend.entities.ForumPost;

public class CategoryStatsDTO {

    private ForumPost.Category category;
    private Long count;

    public CategoryStatsDTO(ForumPost.Category category, Long count) {
        this.category = category;
        this.count = count;
    }

    public ForumPost.Category getCategory() {
        return category;
    }

    public Long getCount() {
        return count;
    }
}
