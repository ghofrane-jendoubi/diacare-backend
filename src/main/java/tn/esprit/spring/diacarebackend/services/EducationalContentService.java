package tn.esprit.spring.diacarebackend.services;

import tn.esprit.spring.diacarebackend.dto.ContentDTO;
import tn.esprit.spring.diacarebackend.dto.ContentSummaryDTO;
import tn.esprit.spring.diacarebackend.entities.AppUser;
import tn.esprit.spring.diacarebackend.entities.EducationalContent;
import tn.esprit.spring.diacarebackend.entities.ContentComment;
import tn.esprit.spring.diacarebackend.entities.ContentLike;
import tn.esprit.spring.diacarebackend.entities.ContentBookmark;
import tn.esprit.spring.diacarebackend.entities.Emotion;
import tn.esprit.spring.diacarebackend.repository.AppUserRepository;
import tn.esprit.spring.diacarebackend.repository.ContentFeedbackRepository;
import tn.esprit.spring.diacarebackend.repository.EducationalContentRepository;
import tn.esprit.spring.diacarebackend.repository.ContentCommentRepository;
import tn.esprit.spring.diacarebackend.repository.ContentLikeRepository;
import tn.esprit.spring.diacarebackend.repository.ContentBookmarkRepository;
import tn.esprit.spring.diacarebackend.repository.PatientEmotionalStateRepository;
import tn.esprit.spring.diacarebackend.entities.PatientEmotionalState;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class EducationalContentService {

    private final EducationalContentRepository contentRepo;
    private final ContentLikeRepository likeRepo;
    private final ContentCommentRepository commentRepo;
    private final ContentBookmarkRepository bookmarkRepo;
    private final AppUserRepository appUserRepo;
    private final PatientEmotionalStateRepository emotionalStateRepo;
    private final ContentFeedbackRepository feedbackRepo;

    public EducationalContentService(
            EducationalContentRepository contentRepo,
            ContentLikeRepository likeRepo,
            ContentCommentRepository commentRepo,
            ContentBookmarkRepository bookmarkRepo,
            AppUserRepository appUserRepo,
            PatientEmotionalStateRepository emotionalStateRepo,
            ContentFeedbackRepository feedbackRepo) {
        this.contentRepo = contentRepo;
        this.likeRepo = likeRepo;
        this.commentRepo = commentRepo;
        this.bookmarkRepo = bookmarkRepo;
        this.appUserRepo = appUserRepo;
        this.emotionalStateRepo = emotionalStateRepo;
        this.feedbackRepo = feedbackRepo;
    }

    public Page<ContentSummaryDTO> getAllContents(int page, int size, Long userId) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return contentRepo.findByIsPublishedTrueOrderByCreatedAtDesc(pageable)
                .map(c -> toSummaryDTO(c, userId));
    }

    public Page<ContentSummaryDTO> getByCategory(String category, int page, int size, Long userId) {
        EducationalContent.Category cat = EducationalContent.Category.valueOf(category.toUpperCase());
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

    public List<ContentSummaryDTO> getRecommendations(Long userId, String diabetesType) {
        List<EducationalContent> publishedContents = contentRepo.findByIsPublishedTrue();
        if (publishedContents.isEmpty()) {
            return List.of();
        }

        String resolvedDiabetesType = resolveDiabetesType(userId, diabetesType);
        Set<Long> seenContentIds = getSeenContentIds(userId);
        Set<String> interestTokens = buildInterestTokens(userId, resolvedDiabetesType);
        Map<Long, Long> sameTypeLikeCounts = getSameTypeLikeCounts(resolvedDiabetesType);

        List<RecommendationCandidate> candidates = new ArrayList<>();
        for (EducationalContent content : publishedContents) {
            if (content == null || content.getId() == null || seenContentIds.contains(content.getId())) {
                continue;
            }

            double score = scoreContent(content, interestTokens, sameTypeLikeCounts, resolvedDiabetesType);
            candidates.add(new RecommendationCandidate(content, score));
        }

        if (candidates.isEmpty()) {
            return fallbackRecommendations(publishedContents, seenContentIds, userId);
        }

        candidates.sort(
                Comparator.comparingDouble(RecommendationCandidate::score).reversed()
                        .thenComparing(c -> safeLocalDateTime(c.content().getCreatedAt()), Comparator.reverseOrder())
                        .thenComparing(c -> c.content().getLikeCount() != null ? c.content().getLikeCount() : 0L, Comparator.reverseOrder())
        );

        return candidates.stream()
                .limit(6)
                .map(c -> toSummaryDTO(c.content(), userId))
                .collect(Collectors.toList());
    }

    public List<ContentSummaryDTO> getUserBookmarks(Long userId) {
        return bookmarkRepo.findByUserId(userId).stream()
                .map(b -> contentRepo.findById(b.getContentId()).orElse(null))
                .filter(c -> c != null)
                .map(c -> toSummaryDTO(c, userId))
                .collect(Collectors.toList());
    }

    // ===== RECOMMANDATIONS ADAPTATIVES BASÉES SUR L'ÉMOTION + POPULARITÉ =====

    public List<ContentSummaryDTO> getAdaptiveRecommendations(Long patientId) {
        // 1. Récupérer l'état émotionnel du patient
        PatientEmotionalState state = emotionalStateRepo.findByPatientId(patientId).orElse(null);
        double emotionalScore = (state != null) ? state.getAverageScore() : 0.0;

        // 2. Déterminer les préférences en fonction de l'état émotionnel
        final EducationalContent.DifficultyLevel preferredDifficulty;
        final EducationalContent.ContentType preferredContentType;
        final boolean patientIsHappy = emotionalScore > 0.3;
        final boolean patientIsSad = emotionalScore < -0.3;

        if (patientIsSad) {
            // Anxiété -> contenus légers, vidéos, niveau débutant
            preferredDifficulty = EducationalContent.DifficultyLevel.BEGINNER;
            preferredContentType = EducationalContent.ContentType.VIDEO;
        } else if (patientIsHappy) {
            // Bien-être -> quiz, articles avancés
            preferredDifficulty = EducationalContent.DifficultyLevel.ADVANCED;
            preferredContentType = EducationalContent.ContentType.QUIZ;
        } else {
            preferredDifficulty = null;
            preferredContentType = null;
        }

        // 3. Récupérer tous les contenus publiés
        List<EducationalContent> allContents = contentRepo.findByIsPublishedTrue();

        // 4. Calculer le score pour chaque contenu (algorithme hybride)
        return allContents.stream()
                .map(c -> {
                    int score = 0;

                    // 🎯 ÉMOTION : Bonus si contenu HAPPY et patient HAPPY
                    Emotion contentEmotion = getDominantEmotion(c.getId());
                    if (patientIsHappy && contentEmotion == Emotion.HAPPY) {
                        score += 15; // Priorité aux contenus positifs
                    } else if (patientIsSad && contentEmotion == Emotion.HAPPY) {
                        score += 12; // Contenus réconfortants pour patients anxieux
                    }

                    // ❤️ POPULARITÉ : Basée sur les likes (x2 pour valoriser l'engagement)
                    Long likeCount = c.getLikeCount() != null ? c.getLikeCount() : 0L;
                    score += likeCount.intValue() * 2;

                    // 👁️ VUES : Bonus pour contenus populaires
                    if (c.getViewCount() != null && c.getViewCount() > 100) {
                        score += 3;
                    }

                    // 📚 DIFFICULTÉ : Bonus si correspond à l'état émotionnel
                    if (preferredDifficulty != null && preferredDifficulty.equals(c.getDifficultyLevel())) {
                        score += 10;
                    }

                    // 🎬 TYPE : Bonus si correspond aux préférences
                    if (preferredContentType != null && preferredContentType.equals(c.getContentType())) {
                        score += 8;
                    }

                    // 🆕 FRAÎCHEUR : Bonus pour contenus récents
                    if (c.getCreatedAt() != null && c.getCreatedAt().isAfter(LocalDateTime.now().minusDays(30))) {
                        score += 5;
                    }

                    return Map.entry(c, score);
                })
                .sorted((e1, e2) -> Integer.compare(e2.getValue(), e1.getValue()))
                .limit(6)
                .map(e -> toSummaryDTO(e.getKey(), patientId))
                .collect(Collectors.toList());
    }

    /**
     * Récupère l'émotion dominante d'un contenu basée sur les feedbacks.
     * Retourne HAPPY si pas de feedbacks (optimiste par défaut).
     */
    private Emotion getDominantEmotion(Long contentId) {
        List<Emotion> emotions = feedbackRepo.findDominantEmotionsByContentId(contentId);
        return emotions.isEmpty() ? Emotion.HAPPY : emotions.get(0);
    }

    // ===== MÉTHODES AJOUTÉES POUR LES COMMENTAIRES =====

    @Transactional
    public ContentComment addComment(Long contentId, String commentText, Long userId, String userName, Long parentCommentId) {
        ContentComment comment = new ContentComment();
        comment.setContentId(contentId);
        comment.setUserId(userId);
        comment.setUserName(userName);
        comment.setCommentText(commentText);
        comment.setParentCommentId(parentCommentId);
        comment.setIsApproved(true);
        comment.setCreatedAt(LocalDateTime.now());
        comment.setLikeCount(0);
        ContentComment saved = commentRepo.save(comment);
        contentRepo.incrementCommentCount(contentId);
        return saved;
    }

    public EducationalContent getContentEntity(Long id) {
        return contentRepo.findById(id).orElse(null);
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
        dto.setAuthorId(c.getAuthorId());
        dto.setAuthorName(c.getAuthorName());
        dto.setViewCount(c.getViewCount() != null ? c.getViewCount() : 0L);
        dto.setLikeCount(c.getLikeCount() != null ? c.getLikeCount() : 0L);
        dto.setCommentCount(c.getCommentCount() != null ? c.getCommentCount() : 0L);
        dto.setReadingTime(c.getReadingTime() != null ? c.getReadingTime() : 5);
        dto.setDifficultyLevel(c.getDifficultyLevel() != null ? c.getDifficultyLevel().name() : "BEGINNER");
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

    private String resolveDiabetesType(Long userId, String diabetesType) {
        if (diabetesType != null && !diabetesType.isBlank()) {
            return normalizeText(diabetesType);
        }
        if (userId == null) {
            return "";
        }
        return appUserRepo.findById(userId)
                .map(AppUser::getDiabetesType)
                .map(this::normalizeText)
                .orElse("");
    }

    private Set<Long> getSeenContentIds(Long userId) {
        if (userId == null) {
            return Set.of();
        }

        Set<Long> seen = new HashSet<>();
        seen.addAll(likeRepo.findByUserId(userId).stream()
                .map(ContentLike::getContentId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet()));
        seen.addAll(bookmarkRepo.findByUserId(userId).stream()
                .map(ContentBookmark::getContentId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet()));
        return seen;
    }

    private Set<String> buildInterestTokens(Long userId, String diabetesType) {
        Set<String> tokens = new LinkedHashSet<>(diabetesTypeTokens(diabetesType));
        if (userId == null) {
            return tokens;
        }

        Set<Long> historyContentIds = new LinkedHashSet<>();
        historyContentIds.addAll(likeRepo.findByUserId(userId).stream()
                .map(ContentLike::getContentId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet()));
        historyContentIds.addAll(bookmarkRepo.findByUserId(userId).stream()
                .map(ContentBookmark::getContentId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet()));

        for (Long contentId : historyContentIds) {
            contentRepo.findById(contentId).ifPresent(content -> {
                tokens.addAll(extractContentTokens(content));
            });
        }

        return tokens;
    }

    private Map<Long, Long> getSameTypeLikeCounts(String diabetesType) {
        if (diabetesType == null || diabetesType.isBlank()) {
            return Map.of();
        }

        List<AppUser> sameTypeUsers = appUserRepo.findByDiabetesTypeIgnoreCase(diabetesType);
        if (sameTypeUsers.isEmpty()) {
            return Map.of();
        }

        Set<Long> sameTypeUserIds = sameTypeUsers.stream()
                .map(AppUser::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (sameTypeUserIds.isEmpty()) {
            return Map.of();
        }

        return likeRepo.findByUserIdIn(sameTypeUserIds).stream()
                .filter(like -> like.getContentId() != null)
                .collect(Collectors.groupingBy(ContentLike::getContentId, Collectors.counting()));
    }

    private double scoreContent(EducationalContent content,
                                Set<String> interestTokens,
                                Map<Long, Long> sameTypeLikeCounts,
                                String diabetesType) {
        Set<String> contentTokens = extractContentTokens(content);
        double score = 0.0;

        long overlap = contentTokens.stream().filter(interestTokens::contains).count();
        score += overlap * 4.0;

        Long sameTypeLikes = sameTypeLikeCounts.get(content.getId());
        if (sameTypeLikes != null) {
            score += Math.min(sameTypeLikes, 10L) * 1.5;
        }

        Long likeCount = content.getLikeCount() != null ? content.getLikeCount() : 0L;
        Long viewCount = content.getViewCount() != null ? content.getViewCount() : 0L;
        Long commentCount = content.getCommentCount() != null ? content.getCommentCount() : 0L;
        score += Math.min(likeCount, 50L) * 0.15;
        score += Math.min(viewCount, 200L) * 0.03;
        score += Math.min(commentCount, 25L) * 0.08;

        if (Boolean.TRUE.equals(content.getIsFeatured())) {
            score += 1.0;
        }

        if (matchesDiabetesType(content, diabetesType)) {
            score += 2.5;
        }

        score += freshnessBonus(content.getCreatedAt());
        return score;
    }

    private List<ContentSummaryDTO> fallbackRecommendations(List<EducationalContent> contents,
                                                            Set<Long> seenContentIds,
                                                            Long userId) {
        return contents.stream()
                .filter(content -> content != null && content.getId() != null && !seenContentIds.contains(content.getId()))
                .sorted(Comparator.comparing((EducationalContent c) -> c.getLikeCount() != null ? c.getLikeCount() : 0L, Comparator.reverseOrder())
                        .thenComparing(c -> c.getViewCount() != null ? c.getViewCount() : 0L, Comparator.reverseOrder())
                        .thenComparing(c -> safeLocalDateTime(c.getCreatedAt()), Comparator.reverseOrder()))
                .limit(6)
                .map(c -> toSummaryDTO(c, userId))
                .collect(Collectors.toList());
    }

    private Set<String> extractContentTokens(EducationalContent content) {
        Set<String> tokens = new HashSet<>();
        if (content.getTags() != null && !content.getTags().isBlank()) {
            tokens.addAll(splitTokens(content.getTags()));
        }
        if (content.getCategory() != null) {
            tokens.addAll(categoryTokens(content.getCategory().name()));
        }
        if (content.getTitle() != null) {
            tokens.addAll(splitTokens(content.getTitle()));
        }
        return tokens;
    }

    private Set<String> categoryTokens(String category) {
        String normalized = normalizeText(category);
        Set<String> tokens = new HashSet<>();
        switch (normalized) {
            case "nutrition" -> tokens.addAll(Set.of("nutrition", "alimentation", "repas", "glycemie"));
            case "exercise" -> tokens.addAll(Set.of("sport", "exercice", "activite", "physique"));
            case "medication" -> tokens.addAll(Set.of("medicament", "traitement", "insuline", "dose"));
            case "monitoring" -> tokens.addAll(Set.of("surveillance", "glycemie", "capteur", "controle"));
            case "lifestyle" -> tokens.addAll(Set.of("mode", "vie", "habitudes", "sommeil", "stress"));
            case "mental_health" -> tokens.addAll(Set.of("stress", "moral", "soutien", "motivation"));
            default -> {
            }
        }
        return tokens;
    }

    private Set<String> diabetesTypeTokens(String diabetesType) {
        Set<String> tokens = new LinkedHashSet<>();
        String normalized = normalizeText(diabetesType);
        if (normalized.contains("type1") || normalized.contains("type 1")) {
            tokens.addAll(Set.of("insuline", "glycemie", "hypoglycemie", "pompe", "surveillance"));
        } else if (normalized.contains("type2") || normalized.contains("type 2")) {
            tokens.addAll(Set.of("alimentation", "sport", "exercice", "poids", "insuline", "glycemie"));
        } else if (normalized.contains("gestationnel")) {
            tokens.addAll(Set.of("grossesse", "alimentation", "surveillance", "glycemie", "suivi"));
        }
        return tokens;
    }

    private boolean matchesDiabetesType(EducationalContent content, String diabetesType) {
        if (diabetesType == null || diabetesType.isBlank()) {
            return false;
        }
        Set<String> contentTokens = extractContentTokens(content);
        Set<String> profileTokens = diabetesTypeTokens(diabetesType);
        for (String token : profileTokens) {
            if (contentTokens.contains(token)) {
                return true;
            }
        }
        return false;
    }

    private Set<String> splitTokens(String raw) {
        if (raw == null || raw.isBlank()) {
            return Set.of();
        }
        return java.util.Arrays.stream(normalizeText(raw).split("[^a-z0-9]+"))
                .filter(token -> !token.isBlank())
                .collect(Collectors.toSet());
    }

    private String normalizeText(String raw) {
        if (raw == null) {
            return "";
        }
        String normalized = Normalizer.normalize(raw, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return normalized.toLowerCase(Locale.ROOT).trim();
    }

    private LocalDateTime safeLocalDateTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime : LocalDateTime.MIN;
    }

    private double freshnessBonus(LocalDateTime createdAt) {
        if (createdAt == null) {
            return 0.0;
        }
        long daysOld = java.time.Duration.between(createdAt, LocalDateTime.now()).toDays();
        if (daysOld <= 0) {
            return 1.2;
        }
        if (daysOld <= 7) {
            return 1.0;
        }
        if (daysOld <= 30) {
            return 0.6;
        }
        return 0.2;
    }

    private record RecommendationCandidate(EducationalContent content, double score) {}

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
        dto.setAuthorId(c.getAuthorId());
        dto.setAuthorName(c.getAuthorName());
        dto.setViewCount(c.getViewCount() != null ? c.getViewCount() + 1L : 1L);
        dto.setLikeCount(c.getLikeCount() != null ? c.getLikeCount() : 0L);
        dto.setCommentCount(c.getCommentCount() != null ? c.getCommentCount() : 0L);
        dto.setReadingTime(c.getReadingTime() != null ? c.getReadingTime() : 5);
        dto.setDifficultyLevel(c.getDifficultyLevel() != null ? c.getDifficultyLevel().name() : "BEGINNER");
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
