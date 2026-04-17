package tn.esprit.spring.diacarebackend.services;

import tn.esprit.spring.diacarebackend.entities.Notification_education;
import tn.esprit.spring.diacarebackend.repository.NotificationeducationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationeducationRepository notificationRepository;

    public NotificationService(NotificationeducationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public Notification_education createNotification(Long doctorId, Notification_education.NotificationType type,
                                         Long contentId, Long commentId, Long triggeredBy,
                                         String triggeredByName, String message) {
        Notification_education notification = new Notification_education();
        notification.setDoctorId(doctorId);
        notification.setType(type);
        notification.setContentId(contentId);
        notification.setCommentId(commentId);
        notification.setTriggeredBy(triggeredBy);
        notification.setTriggeredByName(triggeredByName);
        notification.setMessage(message);
        return notificationRepository.save(notification);
    }

    public List<Notification_education> getNotificationsForDoctor(Long doctorId) {
        return notificationRepository.findByDoctorIdOrderByCreatedAtDesc(doctorId);
    }

    public List<Notification_education> getUnreadNotificationsForDoctor(Long doctorId) {
        return notificationRepository.findByDoctorIdAndIsReadFalseOrderByCreatedAtDesc(doctorId);
    }

    public long getUnreadCountForDoctor(Long doctorId) {
        return notificationRepository.countByDoctorIdAndIsReadFalse(doctorId);
    }

    public void markAsRead(Long notificationId) {
        Notification_education notification = notificationRepository.findById(notificationId).orElse(null);
        if (notification != null) {
            notification.setIsRead(true);
            notificationRepository.save(notification);
        }
    }

    public void markAllAsReadForDoctor(Long doctorId) {
        List<Notification_education> unread = notificationRepository.findByDoctorIdAndIsReadFalseOrderByCreatedAtDesc(doctorId);
        for (Notification_education n : unread) {
            n.setIsRead(true);
        }
        notificationRepository.saveAll(unread);
    }
}
