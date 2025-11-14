package com.example.forgetPassword.respository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.forgetPassword.entity.UserDetails;

public interface UserRepository extends JpaRepository<UserDetails, Integer> {
	Optional<UserDetails> findByUserName(String userName);

	Optional<UserDetails> findByEmail(String email);
}
