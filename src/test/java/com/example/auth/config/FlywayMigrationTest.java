package com.example.auth.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Flyway 마이그레이션 통합 테스트.
 *
 * H2 MySQL 호환 모드 + Flyway 활성화 + ddl-auto=none 조합으로 검증합니다.
 * Hibernate가 DDL을 건드리지 않고, Flyway SQL만으로 스키마가 생성되는지 확인합니다.
 */
@SpringBootTest
@TestPropertySource(properties = {
        "spring.flyway.enabled=true",
        "spring.flyway.locations=classpath:db/migration",
        "spring.datasource.url=jdbc:h2:mem:flywaytest;DB_CLOSE_DELAY=-1;MODE=MySQL;NON_KEYWORDS=VALUE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=none",
        "management.health.mail.enabled=false"
})
class FlywayMigrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockBean
    private JavaMailSender javaMailSender;

    @Test
    @DisplayName("Flyway V1 마이그레이션 후 5개 테이블이 모두 생성된다")
    void allRequiredTablesExistAfterMigration() {
        assertTableExists("users");
        assertTableExists("refresh_tokens");
        assertTableExists("email_verification_tokens");
        assertTableExists("password_reset_tokens");
        assertTableExists("audit_logs");
    }

    @Test
    @DisplayName("users 테이블에 필수 컬럼이 존재한다")
    void usersTableHasRequiredColumns() {
        assertColumnExists("users", "id");
        assertColumnExists("users", "email");
        assertColumnExists("users", "password");
        assertColumnExists("users", "role");
        assertColumnExists("users", "provider");
        assertColumnExists("users", "email_verified");
        assertColumnExists("users", "two_factor_type");
        assertColumnExists("users", "failed_login_attempts");
        assertColumnExists("users", "suspended");
        assertColumnExists("users", "deleted_at");
        assertColumnExists("users", "created_at");
    }

    @Test
    @DisplayName("refresh_tokens 테이블에 token_hash unique 컬럼이 존재한다")
    void refreshTokensTableHasTokenHashColumn() {
        assertColumnExists("refresh_tokens", "token_hash");
        assertColumnExists("refresh_tokens", "email");
        assertColumnExists("refresh_tokens", "expires_at");
    }

    private void assertTableExists(String tableName) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables WHERE UPPER(table_name) = ?",
                Integer.class,
                tableName.toUpperCase()
        );
        assertThat(count).withFailMessage("테이블이 존재하지 않습니다: %s", tableName).isGreaterThan(0);
    }

    private void assertColumnExists(String tableName, String columnName) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.columns " +
                "WHERE UPPER(table_name) = ? AND UPPER(column_name) = ?",
                Integer.class,
                tableName.toUpperCase(),
                columnName.toUpperCase()
        );
        assertThat(count).withFailMessage("컬럼이 존재하지 않습니다: %s.%s", tableName, columnName).isGreaterThan(0);
    }
}
