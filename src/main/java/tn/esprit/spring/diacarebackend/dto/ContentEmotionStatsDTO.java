package tn.esprit.spring.diacarebackend.dto;

public class ContentEmotionStatsDTO {
    private Long contentId;
    private String contentTitle;
    private String category;
    private long feedbackCount;
    private long happyCount;
    private long neutralCount;
    private long sadCount;
    private double anxietyRate;

    public Long getContentId() { return contentId; }
    public String getContentTitle() { return contentTitle; }
    public String getCategory() { return category; }
    public long getFeedbackCount() { return feedbackCount; }
    public long getHappyCount() { return happyCount; }
    public long getNeutralCount() { return neutralCount; }
    public long getSadCount() { return sadCount; }
    public double getAnxietyRate() { return anxietyRate; }

    public void setContentId(Long contentId) { this.contentId = contentId; }
    public void setContentTitle(String contentTitle) { this.contentTitle = contentTitle; }
    public void setCategory(String category) { this.category = category; }
    public void setFeedbackCount(long feedbackCount) { this.feedbackCount = feedbackCount; }
    public void setHappyCount(long happyCount) { this.happyCount = happyCount; }
    public void setNeutralCount(long neutralCount) { this.neutralCount = neutralCount; }
    public void setSadCount(long sadCount) { this.sadCount = sadCount; }
    public void setAnxietyRate(double anxietyRate) { this.anxietyRate = anxietyRate; }
}
