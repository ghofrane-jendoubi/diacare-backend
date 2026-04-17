package tn.esprit.spring.diacarebackend.repository;

import org.springframework.stereotype.Repository;
import tn.esprit.spring.diacarebackend.entities.GlycemieRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
@Repository
public interface GlycemieRecordRepository extends JpaRepository<GlycemieRecord, Long> {

    List<GlycemieRecord> findByPatientIdOrderByMeasuredAtDesc(Long patientId);

    @Query("SELECT g FROM GlycemieRecord g WHERE g.patientId = :patientId " +
            "ORDER BY g.measuredAt DESC")
    List<GlycemieRecord> findLast10ByPatientId(@Param("patientId") Long patientId,
                                               org.springframework.data.domain.Pageable pageable);
}
