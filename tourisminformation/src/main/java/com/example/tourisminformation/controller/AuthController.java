package com.example.tourisminformation.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.tourisminformation.dto.AuthResponse;
import com.example.tourisminformation.dto.LoginRequest;
import com.example.tourisminformation.dto.RegisterRequest;
import com.example.tourisminformation.entity.UserEntity;
import com.example.tourisminformation.repository.UserRepository;
import com.example.tourisminformation.security.JwtTokenProvider;
import com.example.tourisminformation.service.AuditLoggerService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final AuthenticationManager authenticationManager;
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtTokenProvider tokenProvider;
	private final AuditLoggerService auditLogger;

	public AuthController(
			AuthenticationManager authenticationManager,
			UserRepository userRepository,
			PasswordEncoder passwordEncoder,
			JwtTokenProvider tokenProvider,
			AuditLoggerService auditLogger) {
		this.authenticationManager = authenticationManager;
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.tokenProvider = tokenProvider;
		this.auditLogger = auditLogger;
	}

	@PostMapping("/login")
	public ResponseEntity<AuthResponse> login(
			@Valid @RequestBody LoginRequest request,
			HttpServletRequest servletRequest) {
		String clientIp = getClientIp(servletRequest);
		try {
			Authentication authentication = authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(request.username(), request.password()));

			SecurityContextHolder.getContext().setAuthentication(authentication);
			String token = tokenProvider.generateToken(authentication);

			String role = authentication.getAuthorities().stream()
					.map(GrantedAuthority::getAuthority)
					.findFirst()
					.orElse("ROLE_USER");

			auditLogger.logLoginSuccess(request.username(), clientIp, "/api/auth/login");
			return ResponseEntity.ok(new AuthResponse(token, request.username(), role));
		} catch (Exception ex) {
			auditLogger.logLoginFailure(request.username(), clientIp, "/api/auth/login");
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
		}
	}

	@PostMapping("/register")
	public ResponseEntity<AuthResponse> register(
			@Valid @RequestBody RegisterRequest request,
			HttpServletRequest servletRequest) {
		if (userRepository.existsByUsername(request.username())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username is already taken");
		}

		if (userRepository.existsByEmail(request.email())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is already in use");
		}

		String role = "ROLE_USER";
		if (request.role() != null && request.role().trim().equalsIgnoreCase("ADMIN")) {
			role = "ROLE_ADMIN";
		}

		UserEntity user = new UserEntity(
				request.username().trim(),
				passwordEncoder.encode(request.password()),
				request.email().trim(),
				role);

		userRepository.save(user);

		String token = tokenProvider.generateToken(user.getUsername(), user.getRole());
		auditLogger.logLoginSuccess(user.getUsername(), getClientIp(servletRequest), "/api/auth/register");

		return ResponseEntity.status(HttpStatus.CREATED)
				.body(new AuthResponse(token, user.getUsername(), user.getRole()));
	}

	private String getClientIp(HttpServletRequest request) {
		String xfHeader = request.getHeader("X-Forwarded-For");
		if (xfHeader == null || xfHeader.isBlank()) {
			return request.getRemoteAddr();
		}
		return xfHeader.split(",")[0].trim();
	}
}
