package com.example.forgetPassword.config;

import java.util.Arrays;
import java.util.Collections;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.forgetPassword.Exception.CustomException.UserException;
import com.example.forgetPassword.respository.UserRepository;

@Service
public class CustomUserDetailService implements UserDetailsService {

	private UserRepository userRepository;

	public CustomUserDetailService(UserRepository userRepository) {
		super();
		this.userRepository = userRepository;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		String[] info = username.split("-");
		System.err.println("Myuser Details line 29 : " + username + "------>Roles" + Arrays.toString(info));
		if (info[0].equals("user")) {

			return userInfo(info[1]);
		} else {
			return null;
		}
	}

	@Transactional
	private UserDetails userInfo(String email) {

		com.example.forgetPassword.entity.UserDetails userDetails;

		userDetails = userRepository.findByEmail(email).orElseThrow(() -> new UserException("User Not found."));
		if (userDetails != null && userDetails.getEmail().equals(email)) {
			System.out.println("Myuser Details line 109 : " + userDetails.getPassword() + "Userssssss-------->"
					+ userDetails.getEmail());
			return User.withUsername(userDetails.getEmail()).password(userDetails.getPassword())
					.authorities(Collections.emptyList()).build();
		} else {
			throw new UsernameNotFoundException("User not found");
		}

	}

}