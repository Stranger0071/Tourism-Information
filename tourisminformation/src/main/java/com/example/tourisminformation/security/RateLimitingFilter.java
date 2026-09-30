package com.example.tourisminformation.security;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.tourisminformation.service.AuditLoggerService;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {

	private final AuditLoggerService auditLogger;
	private final Map<String, Bucket> loginBuckets = new ConcurrentHashMap<>();
	private final Map<String, Bucket> bookingBuckets = new ConcurrentHashMap<>();

	public RateLimitingFilter(AuditLoggerService auditLogger) {
		this.auditLogger = auditLogger;
	}

	private Bucket createLoginBucket() {
		Bandwidth limit = Bandwidth.builder()
				.capacity(5)
				.refillGreedy(5, Duration.ofMinutes(1))
				.build();
		return Bucket.builder()
				.addLimit(limit)
				.build();
	}

	private Bucket createBookingBucket() {
		Bandwidth limit = Bandwidth.builder()
				.capacity(10)
				.refillGreedy(10, Duration.ofMinutes(1))
				.build();
		return Bucket.builder()
				.addLimit(limit)
				.build();
	}

	@Override
	protected void doFilterInternal(
			HttpServletRequest request,
			HttpServletResponse response,
			FilterChain filterChain) throws ServletException, IOException {

		String path = request.getRequestURI();
		String clientIp = getClientIp(request);
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String username = auth != null ? auth.getName() : "anonymous";

		if (path.equals("/api/auth/login")) {
			Bucket bucket = loginBuckets.computeIfAbsent(clientIp, k -> createLoginBucket());
			if (!bucket.tryConsume(1)) {
				auditLogger.logRateLimitHit(username, clientIp, path);
				response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
				response.setContentType("application/json");
				response.getWriter().write("{\"error\": \"Too many login attempts. Please try again later.\"}");
				return;
			}
		} else if (path.startsWith("/api/booking")) {
			Bucket bucket = bookingBuckets.computeIfAbsent(clientIp, k -> createBookingBucket());
			if (!bucket.tryConsume(1)) {
				auditLogger.logRateLimitHit(username, clientIp, path);
				response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
				response.setContentType("application/json");
				response.getWriter().write("{\"error\": \"Too many booking requests. Please try again later.\"}");
				return;
			}
		}

		filterChain.doFilter(request, response);
	}

	private String getClientIp(HttpServletRequest request) {
		String xfHeader = request.getHeader("X-Forwarded-For");
		if (xfHeader == null || xfHeader.isBlank()) {
			return request.getRemoteAddr();
		}
		return xfHeader.split(",")[0].trim();
	}
}
