package com.example.tourisminformation.service;

import java.util.logging.Logger;

import org.springframework.stereotype.Service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

@Service
public class AuditLoggerService {

	private static final Logger auditLogger = Logger.getLogger("AuditLogger");

	private final Counter loginSuccessCounter;
	private final Counter loginFailureCounter;
	private final Counter accessDeniedCounter;
	private final Counter rateLimitBlockedCounter;

	public AuditLoggerService(MeterRegistry meterRegistry) {
		this.loginSuccessCounter = Counter.builder("audit.login.success")
				.description("Count of successful user logins")
				.register(meterRegistry);

		this.loginFailureCounter = Counter.builder("audit.login.failure")
				.description("Count of failed user login attempts")
				.register(meterRegistry);

		this.accessDeniedCounter = Counter.builder("audit.access.denied")
				.description("Count of access denied (HTTP 403) events")
				.register(meterRegistry);

		this.rateLimitBlockedCounter = Counter.builder("audit.rate_limit.blocked")
				.description("Count of rate-limit blocked (HTTP 429) requests")
				.register(meterRegistry);
	}

	public void logLoginSuccess(String username, String clientIp, String endpoint) {
		loginSuccessCounter.increment();
		auditLogger.info(String.format("AUDIT_EVENT: event=LOGIN_SUCCESS timestamp=%d username=%s ip=%s endpoint=%s",
				System.currentTimeMillis(), sanitize(username), sanitize(clientIp), sanitize(endpoint)));
	}

	public void logLoginFailure(String username, String clientIp, String endpoint) {
		loginFailureCounter.increment();
		auditLogger.warning(String.format("AUDIT_EVENT: event=LOGIN_FAILURE timestamp=%d username=%s ip=%s endpoint=%s",
				System.currentTimeMillis(), sanitize(username), sanitize(clientIp), sanitize(endpoint)));
	}

	public void logAccessDenied(String username, String clientIp, String endpoint) {
		accessDeniedCounter.increment();
		auditLogger.warning(String.format("AUDIT_EVENT: event=ACCESS_DENIED timestamp=%d username=%s ip=%s endpoint=%s",
				System.currentTimeMillis(), sanitize(username), sanitize(clientIp), sanitize(endpoint)));
	}

	public void logRateLimitHit(String username, String clientIp, String endpoint) {
		rateLimitBlockedCounter.increment();
		auditLogger.warning(String.format("AUDIT_EVENT: event=RATE_LIMIT_BLOCKED timestamp=%d username=%s ip=%s endpoint=%s",
				System.currentTimeMillis(), sanitize(username), sanitize(clientIp), sanitize(endpoint)));
	}

	private String sanitize(String input) {
		if (input == null || input.isBlank()) {
			return "anonymous";
		}
		// Strips newlines and quotes to prevent log injection or formatting distortion
		return input.replaceAll("[\\r\\n\\t]", "_").replace("\"", "'");
	}
}
