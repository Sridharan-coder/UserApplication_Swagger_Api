package com.example.forgetPassword.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.example.forgetPassword.dao.UserDetailDAO;
import com.example.forgetPassword.dto.EmailDTO;
import com.example.forgetPassword.dto.SignInDTO;
import com.example.forgetPassword.dto.UserDetailsDTO;
import com.example.forgetPassword.dto.UserOTPDTO;
import com.example.forgetPassword.entity.UserDetails;
import com.example.forgetPassword.response.SuccessDataResponse;
import com.example.forgetPassword.response.SuccessResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;

@CrossOrigin(origins = "*", methods = { RequestMethod.DELETE, RequestMethod.GET, RequestMethod.HEAD,
		RequestMethod.PATCH, RequestMethod.POST, RequestMethod.PUT })
@RestController
@RequestMapping("/v1")
@AllArgsConstructor
@Tag(name = "User Api's", description = "These are the basic api for the User singIn, SignUp, forgetPassowrd.")
public class UserController {

	private static final Logger logger = LoggerFactory.getLogger(UserController.class);
	private UserDetailDAO dao;

	@Operation(summary = "Get SignIn reesult", description = "BY verifying email and password SignIn will perform.")
	@PostMapping(value="/signIn",consumes = "application/json",produces = "application/json")
	public ResponseEntity<SuccessDataResponse> signIn(@Valid @RequestBody SignInDTO user,HttpServletResponse response) {
		Long start = System.currentTimeMillis();
		SuccessDataResponse successResponse = dao.signIn(user.getEmail(), user.getPassword(), response);
		Long end = System.currentTimeMillis();
		logger.info("END: signIn() - Duration: {} ms", (end - start));
		return ResponseEntity.ok(successResponse);
	}

	@Operation(summary = "Get the result of the SignUp.", description = "By verifying email, userName and Passowrd Signup will perform.")
	@PostMapping(value = "/signUp", consumes = "application/json", produces = "application/json")
	public ResponseEntity<SuccessResponse> signUp(@Valid @RequestBody UserDetailsDTO user) {
		Long start = System.currentTimeMillis();
		if (user.getEmail() == null || user.getUserName() == null || user.getPassword() == null) {
			throw new IllegalArgumentException("Email, Username, and Password must not be null.");
		}
		UserDetails userInfo = new UserDetails(user.getEmail(), user.getUserName(), user.getPassword());

		SuccessResponse successResponse = dao.signUp(userInfo);
		Long end = System.currentTimeMillis();
		logger.info("END: signUp() - Duration: {} ms", (end - start));
		return ResponseEntity.status(successResponse.getStatusCode()).body(successResponse);
	}

	@Operation(summary = "Get the result of userName Exists.", description = "To check the userName is Already exits or not.")
	@GetMapping(value = "/{userName}", produces = "application/json")
	public ResponseEntity<SuccessResponse> verifyUserName(@PathVariable @NotBlank String userName) {
		Long start = System.currentTimeMillis();
		SuccessResponse successResponse = dao.verifyUserName(userName);
		Long end = System.currentTimeMillis();
		logger.info("END: verifyUserName() - Duration: {} ms", (end - start));
		return ResponseEntity.status(successResponse.getStatusCode()).body(successResponse);
	}

	@Operation(summary = "Verify the Otp.", description = "By verifying the otp with email is correct or not.")
	@PostMapping(value = "/otpValidation", consumes = "application/json", produces = "application/json")
	public ResponseEntity<SuccessResponse> otpValidation(@RequestBody @Valid UserOTPDTO user) {
		Long start = System.currentTimeMillis();
		if (user.getEmail() == null || user.getOtp() == null) {
			throw new IllegalArgumentException("Email and Otp must not be null.");
		}

		SuccessResponse successResponse = dao.otpValidation(user);
		Long end = System.currentTimeMillis();
		logger.info("END: otpValidation() - Duration: {} ms", (end - start));
		return ResponseEntity.ok(successResponse);
	}

	@Operation(summary = "Get the result of the mail send.", description = "To send the otp mail if it was exists.")
	@PostMapping(value = "/forgetPassword", consumes = "application/json", produces = "application/json")
	public ResponseEntity<SuccessResponse> forgetPassword(@RequestBody EmailDTO request) {
		Long start = System.currentTimeMillis();
		String email = request.getEmail();
		SuccessResponse successResponse = dao.forgetPassword(email);
		Long end = System.currentTimeMillis();
		logger.info("END: forgetPassword() - Duration: {} ms", (end - start));
		return ResponseEntity.ok(successResponse);
	}

	@Operation(summary = "Get the user by id.", description = "To get the User By id.")
	@GetMapping(value = "/user/{id}", produces = "application/json")
	public ResponseEntity<SuccessDataResponse> handleGetUserByID(@PathVariable @Valid Integer id) {
		logger.error("check");
		Long start = System.currentTimeMillis();
		SuccessDataResponse successResponse = dao.getUserByID(id);
		Long end = System.currentTimeMillis();
		logger.info("END: forgetPassword() - Duration: {} ms", (end - start));
		return ResponseEntity.ok(successResponse);
	}

	@Operation(summary = "Updated the User.", description = "To Update the User when the otp is verified.")
	@PostMapping(value = "/update-user/{id}", consumes = "application/json", produces = "application/json")
	public ResponseEntity<SuccessResponse> handleUpdateUser(@Valid @RequestBody UserDetailsDTO user,
			@PathVariable Integer id) {

		Long start = System.currentTimeMillis();
		if (user.getEmail() == null || user.getUserName() == null || user.getPassword() == null) {
			throw new IllegalArgumentException("Email, Username, and Password must not be null.");
		}
		UserDetails userInfo = new UserDetails(user.getEmail(), user.getUserName(), user.getPassword());

		SuccessResponse successResponse = dao.updatedUser(userInfo, id);
		Long end = System.currentTimeMillis();
		logger.info("END: signUp() - Duration: {} ms", (end - start));
		return ResponseEntity.status(successResponse.getStatusCode()).body(successResponse);
	}

}
