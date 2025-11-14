package com.example.forgetPassword.respository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.forgetPassword.entity.UserDetails;
import com.example.forgetPassword.entity.UserOTP;

public interface UserOTPRepository extends JpaRepository<UserOTP, Integer> {
	Optional<UserOTP> findByUser(UserDetails user);
}
