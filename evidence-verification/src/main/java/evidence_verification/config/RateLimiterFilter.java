package evidence_verification.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RateLimiterFilter extends OncePerRequestFilter {

    private static class RequestCounter {
        final long windowStart;
        final AtomicInteger count;

        RequestCounter(long windowStart) {
            this.windowStart = windowStart;
            this.count = new AtomicInteger(1);
        }
    }

    private final Map<String, RequestCounter> requestCounts = new ConcurrentHashMap<>();
    private static final long TIME_WINDOW_MS = 60000; // 1 minute window

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        int maxLimit = getLimitForPath(path);

        if (maxLimit > 0) {
            String clientIp = getClientIP(request);
            String key = path + ":" + clientIp;
            long currentTime = System.currentTimeMillis();

            RequestCounter counter = requestCounts.compute(key, (k, existing) -> {
                if (existing == null || (currentTime - existing.windowStart) > TIME_WINDOW_MS) {
                    return new RequestCounter(currentTime);
                } else {
                    existing.count.incrementAndGet();
                    return existing;
                }
            });

            if (counter.count.get() > maxLimit) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType("application/json");
                response.setHeader("Retry-After", "60");
                response.getWriter().write("{\"error\":\"Too many requests. Please wait a moment before trying again.\"}");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private int getLimitForPath(String path) {
        if (path.startsWith("/api/auth/login")) return 10;
        if (path.startsWith("/api/auth/register")) return 10;
        if (path.startsWith("/api/auth/verify-email")) return 10;
        if (path.startsWith("/api/auth/resend-verification")) return 10;
        if (path.startsWith("/api/public/verify/evidence")) return 30;
        if (path.startsWith("/api/evidence/upload")) return 20;
        return 0; // No strict rate limit for other endpoints
    }

    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty()) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0].trim();
    }
}
