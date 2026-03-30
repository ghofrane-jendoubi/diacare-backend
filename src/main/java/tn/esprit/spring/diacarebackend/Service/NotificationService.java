package tn.esprit.spring.diacarebackend.Service;

import tn.esprit.spring.diacarebackend.entities.Notification;
import tn.esprit.spring.diacarebackend.repository.NotificationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public Notification createNotification(Long doctorId, Notification.NotificationType type,
                                         Long contentId, Long commentId, Long triggeredBy,
                                         String triggeredByName, String message) {
        Notification notification = new Notification();
        notification.setDoctorId(doctorId);
        notification.setType(type);
        notification.setContentId(contentId);
        notification.setCommentId(commentId);
        notification.setTriggeredBy(triggeredBy);
        notification.setTriggeredByName(triggeredByName);
        notification.setMessage(message);
        return notificationRepository.save(notification);
    }

    public List<Notification> getNotificationsForDoctor(Long doctorId) {
        return notificationRepository.findByDoctorIdOrderByCreatedAtDesc(doctorId);
    }

    public List<Notification> getUnreadNotificationsForDoctor(Long doctorId) {
        return notificationRepository.findByDoctorIdAndIsReadFalseOrderByCreatedAtDesc(doctorId);
    }

    public long getUnreadCountForDoctor(Long doctorId) {
        return notificationRepository.countByDoctorIdAndIsReadFalse(doctorId);
    }

    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId).orElse(null);
        if (notification != null) {
            notification.setIsRead(true);
            notificationRepository.save(notification);
        }
    }

    public void markAllAsReadForDoctor(Long doctorId) {
        List<Notification> unread = notificationRepository.findByDoctorIdAndIsReadFalseOrderByCreatedAtDesc(doctorId);
        for (Notification n : unread) {
            n.setIsRead(true);
        }
        notificationRepository.saveAll(unread);
    }
}