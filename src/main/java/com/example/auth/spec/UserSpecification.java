package com.example.auth.spec;

import com.example.auth.domain.Provider;
import com.example.auth.domain.Role;
import com.example.auth.domain.User;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class UserSpecification {

    public static Specification<User> hasEmailContaining(String email) {
        return (root, query, cb) ->
                email == null ? null : cb.like(root.get("email"), "%" + email + "%");
    }

    public static Specification<User> hasRole(Role role) {
        return (root, query, cb) ->
                role == null ? null : cb.equal(root.get("role"), role);
    }

    public static Specification<User> hasProvider(Provider provider) {
        return (root, query, cb) ->
                provider == null ? null : cb.equal(root.get("provider"), provider);
    }

    public static Specification<User> isEmailVerified(Boolean verified) {
        return (root, query, cb) ->
                verified == null ? null : cb.equal(root.get("emailVerified"), verified);
    }

    public static Specification<User> isLocked(Boolean locked) {
        if (locked == null) return null;
        return (root, query, cb) -> {
            if (locked) {
                return cb.and(
                        cb.isNotNull(root.get("lockedUntil")),
                        cb.greaterThan(root.get("lockedUntil"), LocalDateTime.now())
                );
            }
            return cb.or(
                    cb.isNull(root.get("lockedUntil")),
                    cb.lessThanOrEqualTo(root.get("lockedUntil"), LocalDateTime.now())
            );
        };
    }
}
