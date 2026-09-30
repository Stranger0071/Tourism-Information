package com.example.tourisminformation.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.tourisminformation.entity.UserEntity;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

	@Query("SELECT u FROM UserEntity u WHERE u.username = :username")
	Optional<UserEntity> findByUsername(@Param("username") String username);

	@Query("SELECT COUNT(u) > 0 FROM UserEntity u WHERE u.username = :username")
	boolean existsByUsername(@Param("username") String username);

	@Query("SELECT COUNT(u) > 0 FROM UserEntity u WHERE u.email = :email")
	boolean existsByEmail(@Param("email") String email);
}
