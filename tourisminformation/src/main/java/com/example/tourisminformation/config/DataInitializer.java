package com.example.tourisminformation.config;

import java.util.logging.Logger;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.example.tourisminformation.entity.UserEntity;
import com.example.tourisminformation.repository.UserRepository;

@Component
public class DataInitializer implements CommandLineRunner {

	private static final Logger logger = Logger.getLogger(DataInitializer.class.getName());

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public void run(String... args) {
		if (!userRepository.existsByUsername("user")) {
			UserEntity user = new UserEntity(
					"user",
					passwordEncoder.encode("user123"),
					"user@example.com",
					"ROLE_USER");
			userRepository.save(user);
			logger.info("Initialized default USER account ('user')");
		}

		if (!userRepository.existsByUsername("admin")) {
			UserEntity admin = new UserEntity(
					"admin",
					passwordEncoder.encode("admin123"),
					"admin@example.com",
					"ROLE_ADMIN");
			userRepository.save(admin);
			logger.info("Initialized default ADMIN account ('admin')");
		}
	}
}
