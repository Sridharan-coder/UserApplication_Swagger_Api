package com.example.forgetPassword.daoImplementaion;

import java.util.List;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.forgetPassword.Exception.CustomException.InvalidDataException;
import com.example.forgetPassword.Exception.CustomException.UserException;
import com.example.forgetPassword.config.jwtFilter.JwtService;
import com.example.forgetPassword.controller.UserController;
import com.example.forgetPassword.dao.UserDetailDAO;
import com.example.forgetPassword.dto.UserOTPDTO;
import com.example.forgetPassword.entity.UserDetails;
import com.example.forgetPassword.entity.UserOTP;
import com.example.forgetPassword.response.SuccessDataResponse;
import com.example.forgetPassword.response.SuccessResponse;
import com.example.forgetPassword.respository.UserOTPRepository;
import com.example.forgetPassword.respository.UserRepository;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

@Service
public class UserDetailDAOImplementation implements UserDetailDAO {

	private static final Logger logger = LoggerFactory.getLogger(UserController.class);
	private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

	private AuthenticationManager authManager;
	private JwtService jwtService;
	private UserRepository repository;
	private UserOTPRepository otpRepository;
	private final JavaMailSender mailSender;

	private ModelMapper mapper;

	@Value("${spring.mail.username}")
	private String senderEmail;

	public UserDetailDAOImplementation(AuthenticationManager authManager, JwtService jwtService,
			UserRepository repository, UserOTPRepository otpRepository, JavaMailSender mailSender, ModelMapper mapper) {
		this.authManager = authManager;
		this.jwtService = jwtService;
		this.repository = repository;
		this.otpRepository = otpRepository;
		this.mailSender = mailSender;
		this.mapper = mapper;
	}

	@Override
	public SuccessDataResponse signIn(String email, String password, HttpServletResponse response) {
		UserDetails dto = repository.findByEmail(email).orElseThrow(() -> new UserException("User Not Found"));

		Authentication authentication = authManager
				.authenticate(new UsernamePasswordAuthenticationToken("user-" + email, password));

		if (authentication.isAuthenticated()) {
			dto.setPassword(null);
			UserDetails mappedData = mapper.map(dto, UserDetails.class);
			Cookie cookie = new Cookie("u_token", jwtService.generateToken(mappedData));
			cookie.setMaxAge(5 * 360);
			cookie.setSecure(true);
			cookie.setHttpOnly(true);
			cookie.setPath("/");
			response.addCookie(cookie);
			logger.warn(String.format("User Logged in for %s", email));
			return new SuccessDataResponse(true, "User Signed In Successfully.", 200, List.of(mappedData));

		} else {
			throw new InvalidDataException("Incorrect password.");
		}
	}

	@Override
	public SuccessResponse signUp(UserDetails user) {
		Optional<UserDetails> dto = repository.findByEmail(user.getEmail());

		if (dto.isEmpty()) {
			Optional<UserDetails> userNameVerficication = repository.findByUserName(user.getUserName());
			if (userNameVerficication.isEmpty()) {
				user.setPassword(encoder.encode(user.getPassword()));
				repository.save(user);
			} else {
				throw new UserException("UserName Already Exists.");
			}
			logger.warn(String.format("User registered in for %s", user.toString()));
			return new SuccessResponse(true, "User SignedUp Successfully.", 201);
		} else {
			throw new UserException("User Already Exists.");
		}
	}

	@Override
	public SuccessResponse verifyUserName(String userName) {
		Optional<UserDetails> userNameVerficication = repository.findByUserName(userName);
		logger.warn(String.format("User checker userName for %s", userName));
		if (userNameVerficication.isEmpty()) {
			return new SuccessResponse(true, "No User Present in the name " + userName, 200);
		}

		return new SuccessResponse(false, "UserName Already exists.", 400);
	}

	@Override
	public SuccessResponse otpValidation(UserOTPDTO user) {
		Optional<UserDetails> dto = repository.findByEmail(user.getEmail());
		if (dto.isEmpty()) {
			throw new UserException("User Not Found");
		} else {
			UserOTP otpDto = otpRepository.findByUser(dto.get()).orElseThrow(() -> new UserException("User Not Found"));
			if (otpDto.getOtp().intValue() == user.getOtp().intValue()) {
				otpRepository.delete(otpDto);
				logger.warn(String.format("User entered OTP for %s", user.toString()));
				return new SuccessResponse(true, "Otp Validated Successfully.", 200);
			} else {
				throw new InvalidDataException("Invalid Otp.");
			}

		}
	}

	@Override
	public SuccessResponse forgetPassword(String email) {
		try {
			MimeMessage mimeMessage = mailSender.createMimeMessage();

			MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

			String emailSubject = "Reset Password OTP";
			int otp = (int) (Math.random() * 900000) + 100000; // generates 6-digit OTP

			String emailContent = """

					<html>
					<head>
					    <style>
					        body {
					            font-family: Arial, Helvetica, sans-serif;
					            background-color: #f4f6f8;
					            color: #333333;
					            margin: 0;
					            padding: 0;
					        }
					        .container {
					            width: 100%%;
					            max-width: 600px;
					            margin: 30px auto;
					            background-color: #ffffff;
					            border-radius: 8px;
					            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
					            padding: 30px;
					        }
					        h2 {
					            color: #0a66c2;
					            text-align: center;
					        }
					        .otp-box {
					            background-color: #f0f8ff;
					            border: 1px dashed #0a66c2;
					            border-radius: 6px;
					            text-align: center;
					            font-size: 28px;
					            letter-spacing: 4px;
					            color: #0a66c2;
					            padding: 15px;
					            margin: 25px 0;
					            font-weight: bold;
					        }
					        p {
					            font-size: 15px;
					            line-height: 1.6;
					            color: #444444;
					        }
					        .warning {
					            background-color: #fff3cd;
					            border: 1px solid #ffeeba;
					            border-radius: 6px;
					            padding: 12px;
					            color: #856404;
					            font-size: 14px;
					            margin-top: 20px;
					        }
					        .footer {
					            text-align: center;
					            font-size: 13px;
					            color: #888888;
					            margin-top: 25px;
					        }
					    </style>
					</head>
					<body>
					    <div class="container">
					        <h2>Reset Your Password</h2>
					        <p>Dear User,</p>
					        <p>We received a request to reset your password. Use the following One-Time Password (OTP) to proceed with your password reset:</p>

					        <div class="otp-box">"""
					+ otp
					+ """
									</div>
							<p>This OTP is valid for the next <strong>10 minutes</strong>. Please do not share it with anyone for security reasons.</p>

							<div class="warning">
							    ⚠️ <strong>Security Reminder:</strong> Never share this OTP with anyone. Our team will never ask for your OTP, password, or any sensitive information.
							</div>

							<p>If you did not request a password reset, please ignore this email. Your account remains secure.</p>

							<div class="footer">
							    © """
					+ java.time.Year.now() + """
							            MitrahSoft Technologies. All rights reserved.
							        </div>
							    </div>
							</body>
							</html>
							""";

			helper.setFrom(senderEmail);
			helper.setTo(email);
			helper.setSubject(emailSubject);
			helper.setText(emailContent, true);
			UserDetails dto = repository.findByEmail(email).orElseThrow(() -> new UserException("User not Found."));

			UserOTP userOtp = otpRepository.findByUser(dto).orElseGet(() -> new UserOTP());

			userOtp.setUser(dto);
			userOtp.setOtp(otp);

			otpRepository.saveAndFlush(userOtp);

			mailSender.send(mimeMessage);
			logger.warn(String.format("Rest pasword for %s", email));
			return new SuccessResponse(true, "Otp send Successfully to " + email, 200);
		} catch (MessagingException e) {
			System.err.println(e.getMessage());
			throw new RuntimeException("Something Went wrong.");
		}
	}

	@Override
	public SuccessDataResponse getUserByID(Integer id) {
		UserDetails user = repository.findById(id).orElseThrow(() -> new UserException("User Not Found."));
		UserDetails mappedData = mapper.map(user, UserDetails.class);
		user.setPassword(null);
		logger.warn(String.format("get User By id for %s", mappedData.toString()));
		return new SuccessDataResponse(true, "User Signed In Successfully.", 200, List.of(mappedData));
	}

	@Override
	public SuccessResponse updatedUser(UserDetails user, Integer id) {

		return null;
	}

}
