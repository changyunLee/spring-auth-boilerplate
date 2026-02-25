package com.example.auth.service;

import com.example.auth.domain.AuditLog;
import com.example.auth.dto.AuditLogDto;
import com.example.auth.repository.AuditLogRepository;
import com.example.auth.spec.AuditLogSpecification;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.PrintWriter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public void log(String action, String targetEmail, String details) {
        auditLogRepository.save(AuditLog.builder()
                .action(action)
                .targetEmail(targetEmail)
                .ipAddress(getClientIp())
                .details(details)
                .build());
    }

    @Transactional(readOnly = true)
    public Page<AuditLogDto.AuditLogResponse> searchAuditLogs(
            AuditLogDto.AuditLogSearchRequest request, Pageable pageable) {
        Specification<AuditLog> spec = Specification
                .where(AuditLogSpecification.hasAction(request.getAction()))
                .and(AuditLogSpecification.hasTargetEmailContaining(request.getTargetEmail()))
                .and(AuditLogSpecification.createdAfter(request.getStartDate()))
                .and(AuditLogSpecification.createdBefore(request.getEndDate()));
        return auditLogRepository.findAll(spec, pageable).map(AuditLogDto.AuditLogResponse::from);
    }

    @Transactional(readOnly = true)
    public void exportAuditLogsCsv(AuditLogDto.AuditLogSearchRequest request, PrintWriter writer) {
        Specification<AuditLog> spec = Specification
                .where(AuditLogSpecification.hasAction(request.getAction()))
                .and(AuditLogSpecification.hasTargetEmailContaining(request.getTargetEmail()))
                .and(AuditLogSpecification.createdAfter(request.getStartDate()))
                .and(AuditLogSpecification.createdBefore(request.getEndDate()));

        List<AuditLog> logs = auditLogRepository.findAll(spec);

        writer.println("ID,Action,TargetEmail,IpAddress,Details,CreatedAt");
        for (AuditLog log : logs) {
            String details = log.getDetails() != null ? log.getDetails().replace("\"", "\"\"") : "";
            writer.printf("%d,%s,%s,%s,\"%s\",%s\n",
                    log.getId(), log.getAction(), log.getTargetEmail() != null ? log.getTargetEmail() : "",
                    log.getIpAddress() != null ? log.getIpAddress() : "",
                    details, log.getCreatedAt());
        }
    }

    private String getClientIp() {
        try {
            HttpServletRequest request = ((ServletRequestAttributes)
                    RequestContextHolder.currentRequestAttributes()).getRequest();
            String xfHeader = request.getHeader("X-Forwarded-For");
            if (xfHeader != null) return xfHeader.split(",")[0].trim();
            return request.getRemoteAddr();
        } catch (Exception e) {
            return "unknown";
        }
    }
}
