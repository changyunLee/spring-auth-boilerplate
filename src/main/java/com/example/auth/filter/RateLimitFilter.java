package com.example.auth.filter;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    // IP:URI 조합별 버킷 캐시 (TTL: 30분 미사용 시 자동 만료)
    private final Cache<String, Bucket> cache = Caffeine.newBuilder()
            .maximumSize(10_000)
            .expireAfterAccess(Duration.ofMinutes(30))
            .build();

    // 엔드포인트별 분당 최대 요청 수 (POST만 적용)
    private static final Map<String, Integer> RATE_LIMITS = Map.of(
            "/api/auth/login",               5,   // 분당 5회 (계정 잠금과 이중 방어)
            "/api/auth/login/2fa",           10,  // 분당 10회 (2FA 코드 재입력 허용)
            "/api/auth/forgot-password",     3,   // 분당 3회 (이메일 폭격 방지)
            "/api/auth/resend-verification", 3    // 분당 3회 (이메일 폭격 방지)
    );

    private Bucket resolveBucket(String cacheKey, int limit) {
        return cache.get(cacheKey, k -> {
            Bandwidth bandwidth = Bandwidth.classic(limit, Refill.intervally(limit, Duration.ofMinutes(1)));
            return Bucket.builder().addLimit(bandwidth).build();
        });
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String uri = request.getRequestURI();
        Integer limit = RATE_LIMITS.get(uri);

        if (limit != null && "POST".equals(request.getMethod())) {
            String ip = getClientIP(request);
            String cacheKey = uri + ":" + ip;
            Bucket bucket = resolveBucket(cacheKey, limit);

            if (!bucket.tryConsume(1)) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"message\": \"너무 많은 요청입니다. 1분 후에 다시 시도해주세요.\"}");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        // 마지막 IP 사용: 신뢰할 수 있는 리버스 프록시가 추가한 실제 클라이언트 IP
        // (첫 번째 IP는 클라이언트가 위조 가능)
        String[] ips = xfHeader.split(",");
        return ips[ips.length - 1].trim();
    }
}
