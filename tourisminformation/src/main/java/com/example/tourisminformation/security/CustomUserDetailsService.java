package com.example.tourisminformation.security;

import java.util.List;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.tourisminformation.entity.UserEntity;
import com.example.tourisminformation.repository.UserRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

	private final UserRepository userRepository;

	public CustomUserDetailsService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		UserEntity userEntity = userRepository.findByUsername(username)
				.orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

		String role = userEntity.getRole();
		if (role == null || role.isBlank()) {
			role = "ROLE_USER";
		} else if (!role.startsWith("ROLE_")) {
			role = "ROLE_" + role;
		}

		return new User(
				userEntity.getUsername(),
				userEntity.getPassword(),
				List.of(new SimpleGrantedAuthority(role)));
	}
}
