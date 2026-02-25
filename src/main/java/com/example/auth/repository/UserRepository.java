package com.example.auth.repository;

import com.example.auth.domain.Provider;
import com.example.auth.domain.Role;
import com.example.auth.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    long countByRole(Role role);
    long countByEmailVerified(boolean emailVerified);
    long countByLockedUntilAfter(LocalDateTime dateTime);
    long countBySuspendedTrue();
    long countByProvider(Provider provider);
    long countByCreatedAtAfter(LocalDateTime dateTime);

    @Query("SELECT CAST(u.createdAt AS date), COUNT(u) FROM User u WHERE u.createdAt >= :since GROUP BY CAST(u.createdAt AS date) ORDER BY CAST(u.createdAt AS date)")
    List<Object[]> countByCreatedAtGroupByDate(@Param("since") LocalDateTime since);
}
