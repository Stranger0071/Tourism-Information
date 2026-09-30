package com.example.tourisminformation.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import io.micrometer.core.instrument.MeterRegistry;

@SpringBootTest
class AuditLoggerServiceTest {

	@Autowired
	private AuditLoggerService auditLoggerService;

	@Autowired
	private MeterRegistry meterRegistry;

	@Test
	void auditEventsIncrementMicrometerCounters() {
		double initialSuccess = meterRegistry.counter("audit.login.success").count();
		double initialFailure = meterRegistry.counter("audit.login.failure").count();
		double initialAccessDenied = meterRegistry.counter("audit.access.denied").count();
		double initialRateLimit = meterRegistry.counter("audit.rate_limit.blocked").count();

		auditLoggerService.logLoginSuccess("testuser", "127.0.0.1", "/api/auth/login");
		auditLoggerService.logLoginFailure("testuser", "127.0.0.1", "/api/auth/login");
		auditLoggerService.logAccessDenied("testuser", "127.0.0.1", "/api/admin/attractions");
		auditLoggerService.logRateLimitHit("testuser", "127.0.0.1", "/api/auth/login");

		assertEquals(initialSuccess + 1, meterRegistry.counter("audit.login.success").count());
		assertEquals(initialFailure + 1, meterRegistry.counter("audit.login.failure").count());
		assertEquals(initialAccessDenied + 1, meterRegistry.counter("audit.access.denied").count());
		assertEquals(initialRateLimit + 1, meterRegistry.counter("audit.rate_limit.blocked").count());
	}
}
