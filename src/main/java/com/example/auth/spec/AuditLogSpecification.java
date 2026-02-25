package com.example.auth.spec;

import com.example.auth.domain.AuditLog;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class AuditLogSpecification {

    public static Specification<AuditLog> hasAction(String action) {
        return (root, query, cb) ->
                action == null ? null : cb.equal(root.get("action"), action);
    }

    public static Specification<AuditLog> hasTargetEmailContaining(String email) {
        return (root, query, cb) ->
                email == null ? null : cb.like(root.get("targetEmail"), "%" + email + "%");
    }

    public static Specification<AuditLog> createdAfter(LocalDateTime start) {
        return (root, query, cb) ->
                start == null ? null : cb.greaterThanOrEqualTo(root.get("createdAt"), start);
    }

    public static Specification<AuditLog> createdBefore(LocalDateTime end) {
        return (root, query, cb) ->
                end == null ? null : cb.lessThanOrEqualTo(root.get("createdAt"), end);
    }
}
