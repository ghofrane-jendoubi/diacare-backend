package tn.esprit.spring.diacarebackend.DTOs;

import java.util.List;

public class EmotionalDashboardDTO {
    private long totalFeedbacks;
    private long happyCount;
    private long neutralCount;
    private long sadCount;
    private List<ContentEmotionStatsDTO> contentStats;

    public long getTotalFeedbacks() { return totalFeedbacks; }
    public long getHappyCount() { return happyCount; }
    public long getNeutralCount() { return neutralCount; }
    public long getSadCount() { return sadCount; }
    public List<ContentEmotionStatsDTO> getContentStats() { return contentStats; }

    public void setTotalFeedbacks(long totalFeedbacks) { this.totalFeedbacks = totalFeedbacks; }
    public void setHappyCount(long happyCount) { this.happyCount = happyCount; }
    public void setNeutralCount(long neutralCount) { this.neutralCount = neutralCount; }
    public void setSadCount(long sadCount) { this.sadCount = sadCount; }
    public void setContentStats(List<ContentEmotionStatsDTO> contentStats) { this.contentStats = contentStats; }
}
