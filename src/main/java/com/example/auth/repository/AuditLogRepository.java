package com.example.auth.repository;

import com.example.auth.domain.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long>, JpaSpecificationExecutor<AuditLog> {

    long countByActionAndCreatedAtAfter(String action, LocalDateTime since);
    int deleteByCreatedAtBefore(LocalDateTime dateTime);

    @Query("SELECT CAST(a.createdAt AS date), COUNT(a) FROM AuditLog a WHERE a.action = :action AND a.createdAt >= :since GROUP BY CAST(a.createdAt AS date) ORDER BY CAST(a.createdAt AS date)")
    List<Object[]> countByActionGroupByDate(@Param("action") String action, @Param("since") LocalDateTime since);

    @Query("SELECT COUNT(DISTINCT a.ipAddress) FROM AuditLog a WHERE a.action = :action AND a.createdAt >= :since")
    long countDistinctIpByActionSince(@Param("action") String action, @Param("since") LocalDateTime since);

    @Query("SELECT a.ipAddress, COUNT(a), MAX(a.createdAt) FROM AuditLog a WHERE a.action = :action AND a.createdAt >= :since GROUP BY a.ipAddress ORDER BY COUNT(a) DESC")
    List<Object[]> countByActionGroupByIp(@Param("action") String action, @Param("since") LocalDateTime since);

    @Query("SELECT COUNT(DISTINCT a.targetEmail) FROM AuditLog a WHERE a.action = :action AND a.createdAt >= :since")
    long countDistinctEmailByActionSince(@Param("action") String action, @Param("since") LocalDateTime since);
}
