package com.example.forgetPassword.dao;

import com.example.forgetPassword.dto.UserOTPDTO;
import com.example.forgetPassword.entity.UserDetails;
import com.example.forgetPassword.response.SuccessDataResponse;
import com.example.forgetPassword.response.SuccessResponse;

import jakarta.servlet.http.HttpServletResponse;

public interface UserDetailDAO {

	SuccessDataResponse signIn(String email, String password,HttpServletResponse response);

	SuccessResponse verifyUserName(String userName);

	SuccessResponse signUp(UserDetails user);

	SuccessResponse forgetPassword(String email);

	SuccessResponse otpValidation(UserOTPDTO user);
	
	SuccessDataResponse getUserByID(Integer id);
	
	SuccessResponse updatedUser(UserDetails user,Integer id);
}
