package com.example.tourisminformation.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.example.tourisminformation.security.CustomAccessDeniedHandler;
import com.example.tourisminformation.security.JwtAuthenticationFilter;
import com.example.tourisminformation.security.RateLimitingFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

	private final JwtAuthenticationFilter jwtAuthenticationFilter;
	private final RateLimitingFilter rateLimitingFilter;
	private final CustomAccessDeniedHandler accessDeniedHandler;
	private final String allowedOrigins;

	public SecurityConfig(
			JwtAuthenticationFilter jwtAuthenticationFilter,
			RateLimitingFilter rateLimitingFilter,
			CustomAccessDeniedHandler accessDeniedHandler,
			@Value("${app.cors.allowed-origins:http://localhost:5173,http://localhost:3000,http://localhost}") String allowedOrigins) {
		this.jwtAuthenticationFilter = jwtAuthenticationFilter;
		this.rateLimitingFilter = rateLimitingFilter;
		this.accessDeniedHandler = accessDeniedHandler;
		this.allowedOrigins = allowedOrigins;
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
		return authConfig.getAuthenticationManager();
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		List<String> origins = Arrays.stream(allowedOrigins.split(","))
				.map(String::trim)
				.filter(s -> !s.isBlank())
				.toList();
		configuration.setAllowedOrigins(origins);
		configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
		configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With", "Accept", "Origin"));
		configuration.setExposedHeaders(List.of("Authorization"));
		configuration.setAllowCredentials(true);
		configuration.setMaxAge(3600L);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/api/**", configuration);
		return source;
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
				.csrf(csrf -> csrf.disable())
				.cors(cors -> cors.configurationSource(corsConfigurationSource()))
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.exceptionHandling(exceptions -> exceptions.accessDeniedHandler(accessDeniedHandler))
				.headers(headers -> headers
						.contentSecurityPolicy(csp -> csp
								.policyDirectives("default-src 'self'; script-src 'self'; style-src 'self' 'unsafe-inline'; img-src 'self' data:; connect-src 'self'"))
						.httpStrictTransportSecurity(hsts -> hsts
								.includeSubDomains(true)
								.maxAgeInSeconds(31536000))
						.frameOptions(frame -> frame.deny())
						.contentTypeOptions(contentType -> {})
						.referrerPolicy(referrer -> referrer
								.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
						.permissionsPolicy(permissions -> permissions
								.policy("geolocation=(), camera=(), microphone=()")))
				.authorizeHttpRequests(auth -> auth
						.requestMatchers("/api/auth/**").permitAll()
						.requestMatchers("/api", "/api/", "/api/health").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/attractions/**").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/hotels/**").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/guides/**").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/maps/**").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/booking", "/api/booking/options/**").permitAll()
						.requestMatchers("/api/admin/**").hasRole("ADMIN")
						.requestMatchers(HttpMethod.POST, "/api/attractions", "/api/attractions/**").hasRole("ADMIN")
						.requestMatchers(HttpMethod.PUT, "/api/attractions/**").hasRole("ADMIN")
						.requestMatchers(HttpMethod.DELETE, "/api/attractions/**").hasRole("ADMIN")
						.requestMatchers(HttpMethod.POST, "/api/hotels", "/api/hotels/**").hasRole("ADMIN")
						.requestMatchers(HttpMethod.PUT, "/api/hotels/**").hasRole("ADMIN")
						.requestMatchers(HttpMethod.DELETE, "/api/hotels/**").hasRole("ADMIN")
						.requestMatchers(HttpMethod.POST, "/api/guides", "/api/guides/**").hasRole("ADMIN")
						.requestMatchers(HttpMethod.PUT, "/api/guides/**").hasRole("ADMIN")
						.requestMatchers(HttpMethod.DELETE, "/api/guides/**").hasRole("ADMIN")
						.anyRequest().authenticated())
				.addFilterBefore(rateLimitingFilter, UsernamePasswordAuthenticationFilter.class)
				.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}
}
