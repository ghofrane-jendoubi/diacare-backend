package tn.esprit.spring.diacarebackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.esprit.spring.diacarebackend.entities.Appointment;
import tn.esprit.spring.diacarebackend.entities.User;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByDoctorId(Long doctorId);

    List<Appointment> findByPatientId(Long patientId);

    @Query("SELECT a FROM Appointment a WHERE a.doctor.id = :doctorId AND a.startTime BETWEEN :start AND :end")
    List<Appointment> findDoctorAppointmentsBetween(
            @Param("doctorId") Long doctorId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("SELECT DISTINCT u FROM User u WHERE u.id IN " +
            "(SELECT DISTINCT m.sender.id FROM Message m WHERE m.receiver.id = :doctorId) " +
            "OR u.id IN (SELECT DISTINCT m.receiver.id FROM Message m WHERE m.sender.id = :doctorId)")
    List<User> findPatientsWithConversations(@Param("doctorId") Long doctorId);
}