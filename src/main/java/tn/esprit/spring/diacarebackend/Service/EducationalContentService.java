package tn.esprit.spring.diacarebackend.Service;

import tn.esprit.spring.diacarebackend.DTOs.ContentDTO;
import tn.esprit.spring.diacarebackend.DTOs.ContentSummaryDTO;
import tn.esprit.spring.diacarebackend.entities.*;
import tn.esprit.spring.diacarebackend.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EducationalContentService {

    private final EducationalContentRepository contentRepo;
    private final ContentLikeRepository likeRepo;
    private final ContentCommentRepository commentRepo;
    private final ContentBookmarkRepository bookmarkRepo;

    public EducationalContentService(
            EducationalContentRepository contentRepo,
            ContentLikeRepository likeRepo,
            ContentCommentRepository commentRepo,
            ContentBookmarkRepository bookmarkRepo) {
        this.contentRepo = contentRepo;
        this.likeRepo = likeRepo;
        this.commentRepo = commentRepo;
        this.bookmarkRepo = bookmarkRepo;
    }

    public Page<ContentSummaryDTO> getAllContents(int page, int size, Long userId) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return contentRepo.findByIsPublishedTrueOrderByCreatedAtDesc(pageable)
                .map(c -> toSummaryDTO(c, userId));
    }

    public Page<ContentSummaryDTO> getByCategory(String category, int page, int size, Long userId) {
        EducationalContent.Category cat =
                EducationalContent.Category.valueOf(category.toUpperCase());
        return contentRepo.findByCategoryAndIsPublishedTrue(cat, PageRequest.of(page, size))
                .map(c -> toSummaryDTO(c, userId));
    }

    public Page<ContentSummaryDTO> search(String keyword, int page, int size, Long userId) {
        return contentRepo.searchContent(keyword, PageRequest.of(page, size))
                .map(c -> toSummaryDTO(c, userId));
    }

    @Transactional
    public ContentDTO getContentDetail(Long id, Long userId) {
        EducationalContent content = contentRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Contenu non trouvé"));
        contentRepo.incrementViewCount(id);
        return toDetailDTO(content, userId);
    }

    @Transactional
    public boolean toggleLike(Long contentId, Long userId) {
        if (likeRepo.existsByContentIdAndUserId(contentId, userId)) {
            likeRepo.deleteByContentIdAndUserId(contentId, userId);
            contentRepo.decrementLikeCount(contentId);
            return false;
        } else {
            ContentLike like = new ContentLike();
            like.setContentId(contentId);
            like.setUserId(userId);
            likeRepo.save(like);
            contentRepo.incrementLikeCount(contentId);
            return true;
        }
    }

    @Transactional
    public boolean toggleBookmark(Long contentId, Long userId) {
        if (bookmarkRepo.existsByContentIdAndUserId(contentId, userId)) {
            bookmarkRepo.deleteByContentIdAndUserId(contentId, userId);
            return false;
        } else {
            ContentBookmark bookmark = new ContentBookmark();
            bookmark.setContentId(contentId);
            bookmark.setUserId(userId);
            bookmarkRepo.save(bookmark);
            return true;
        }
    }

    public List<ContentSummaryDTO> getFeaturedContents(Long userId) {
        return contentRepo.findByIsFeaturedTrueAndIsPublishedTrue()
                .stream()
                .map(c -> toSummaryDTO(c, userId))
                .collect(Collectors.toList());
    }

    public List<ContentSummaryDTO> getMostViewed(Long userId) {
        return contentRepo.findTop5ByIsPublishedTrueOrderByViewCountDesc()
                .stream()
                .map(c -> toSummaryDTO(c, userId))
                .collect(Collectors.toList());
    }

    public List<ContentSummaryDTO> getUserBookmarks(Long userId) {
        return bookmarkRepo.findByUserId(userId).stream()
                .map(b -> contentRepo.findById(b.getContentId()).orElse(null))
                .filter(c -> c != null)
                .map(c -> toSummaryDTO(c, userId))
                .collect(Collectors.toList());
    }

    // ===== HELPERS =====
    private ContentSummaryDTO toSummaryDTO(EducationalContent c, Long userId) {
        ContentSummaryDTO dto = new ContentSummaryDTO();
        dto.setId(c.getId());
        dto.setTitle(c.getTitle() != null ? c.getTitle() : "");
        dto.setSummary(c.getSummary());
        dto.setCategory(c.getCategory() != null ? c.getCategory().name() : "");
        dto.setContentType(c.getContentType() != null ? c.getContentType().name() : "ARTICLE");
        dto.setThumbnailUrl(c.getThumbnailUrl());
        dto.setAuthorName(c.getAuthorName());
        dto.setViewCount(c.getViewCount() != null ? c.getViewCount() : 0L);
        dto.setLikeCount(c.getLikeCount() != null ? c.getLikeCount() : 0L);
        dto.setCommentCount(c.getCommentCount() != null ? c.getCommentCount() : 0L);
        dto.setReadingTime(c.getReadingTime() != null ? c.getReadingTime() : 5);
        dto.setDifficultyLevel(c.getDifficultyLevel() != null ?
                c.getDifficultyLevel().name() : "BEGINNER");
        dto.setIsFeatured(c.getIsFeatured() != null ? c.getIsFeatured() : false);
        dto.setTags(c.getTags());
        dto.setCreatedAt(c.getCreatedAt());
        dto.setIsPublished(c.getIsPublished());
        if (userId != null) {
            dto.setIsLiked(likeRepo.existsByContentIdAndUserId(c.getId(), userId));
            dto.setIsBookmarked(bookmarkRepo.existsByContentIdAndUserId(c.getId(), userId));
        } else {
            dto.setIsLiked(false);
            dto.setIsBookmarked(false);
        }
        return dto;
    }

    private ContentDTO toDetailDTO(EducationalContent c, Long userId) {
        ContentDTO dto = new ContentDTO();
        dto.setId(c.getId());
        dto.setTitle(c.getTitle() != null ? c.getTitle() : "");
        dto.setSubtitle(c.getSubtitle());
        dto.setContent(c.getContent());
        dto.setSummary(c.getSummary());
        dto.setCategory(c.getCategory() != null ? c.getCategory().name() : "");
        dto.setContentType(c.getContentType() != null ? c.getContentType().name() : "ARTICLE");
        dto.setThumbnailUrl(c.getThumbnailUrl());
        dto.setVideoUrl(c.getVideoUrl());
        dto.setAuthorName(c.getAuthorName());
        dto.setViewCount(c.getViewCount() != null ? c.getViewCount() + 1L : 1L);
        dto.setLikeCount(c.getLikeCount() != null ? c.getLikeCount() : 0L);
        dto.setCommentCount(c.getCommentCount() != null ? c.getCommentCount() : 0L);
        dto.setReadingTime(c.getReadingTime() != null ? c.getReadingTime() : 5);
        dto.setDifficultyLevel(c.getDifficultyLevel() != null ?
                c.getDifficultyLevel().name() : "BEGINNER");
        dto.setIsFeatured(c.getIsFeatured() != null ? c.getIsFeatured() : false);
        dto.setTags(c.getTags());
        dto.setCreatedAt(c.getCreatedAt());
        if (userId != null) {
            dto.setIsLiked(likeRepo.existsByContentIdAndUserId(c.getId(), userId));
            dto.setIsBookmarked(bookmarkRepo.existsByContentIdAndUserId(c.getId(), userId));
        } else {
            dto.setIsLiked(false);
            dto.setIsBookmarked(false);
        }
        return dto;
    }
}