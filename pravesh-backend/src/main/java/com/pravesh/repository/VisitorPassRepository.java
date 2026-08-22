package com.pravesh.repository;

import com.pravesh.entity.VisitorPass;
import com.pravesh.entity.enums.PassStatus;
import com.pravesh.entity.enums.PassType;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface VisitorPassRepository extends JpaRepository<VisitorPass, Long> {

    Optional<VisitorPass> findByUuid(String uuid);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT vp FROM VisitorPass vp WHERE vp.uuid = :uuid")
    Optional<VisitorPass> findByUuidForUpdate(@Param("uuid") String uuid);

    List<VisitorPass> findByResident_UserIdAndStatus(Long residentId, PassStatus status);

    List<VisitorPass> findByResident_UserId(Long residentId);

    @Modifying
    @Query("UPDATE VisitorPass vp SET vp.status = 'EXPIRED' " +
           "WHERE vp.status = 'ACTIVE' AND vp.validUntil < :now")
    int expirePassesPastDue(@Param("now") LocalDateTime now);

    List<VisitorPass> findByPassTypeAndStatusIn(PassType passType, List<PassStatus> statuses);

    List<VisitorPass> findByResident_UserIdAndStatusAndSocietyId(
            Long residentId, PassStatus status, Long societyId);

    List<VisitorPass> findByResident_UserIdAndSocietyId(Long residentId, Long societyId);

    List<VisitorPass> findBySocietyId(Long societyId);
}
