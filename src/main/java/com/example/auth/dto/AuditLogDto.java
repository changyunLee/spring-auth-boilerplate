package com.example.auth.dto;

import com.example.auth.domain.AuditLog;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

public class AuditLogDto {

    @Getter
    public static class AuditLogResponse {
        private final Long id;
        private final String action;
        private final String targetEmail;
        private final String ipAddress;
        private final String details;
        private final LocalDateTime createdAt;

        private AuditLogResponse(Long id, String action, String targetEmail,
                                 String ipAddress, String details, LocalDateTime createdAt) {
            this.id = id;
            this.action = action;
            this.targetEmail = targetEmail;
            this.ipAddress = ipAddress;
            this.details = details;
            this.createdAt = createdAt;
        }

        public static AuditLogResponse from(AuditLog log) {
            return new AuditLogResponse(
                    log.getId(),
                    log.getAction(),
                    log.getTargetEmail(),
                    log.getIpAddress(),
                    log.getDetails(),
                    log.getCreatedAt()
            );
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class AuditLogSearchRequest {
        private String action;
        private String targetEmail;

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        private LocalDateTime startDate;

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        private LocalDateTime endDate;
    }
}
