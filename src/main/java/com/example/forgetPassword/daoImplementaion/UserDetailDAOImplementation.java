package com.example.forgetPassword.daoImplementaion;

import java.time.DateTimeException;
import java.util.Date;
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

		if (!authentication.isAuthenticated())
			throw new InvalidDataException("Incorrect password.");

		UserDetails mappedData = mapper.map(dto, UserDetails.class);
		mappedData.setPassword(null);
		Cookie cookie = new Cookie("u_token", jwtService.generateToken(mappedData));
		cookie.setMaxAge(5 * 360);
		cookie.setSecure(true);
		cookie.setHttpOnly(true);
		cookie.setPath("/");
		response.addCookie(cookie);
		logger.warn("User Logged in for {}", email);
		return new SuccessDataResponse(true, "User Signed In Successfully.", 200, List.of(mappedData));

	}

	@Override
	public SuccessResponse signUp(UserDetails user) {
		Optional<UserDetails> dto = repository.findByEmail(user.getEmail());

		if (dto.isPresent())
			throw new UserException("Email Already Exists.");

		if (repository.findByUserName(user.getUserName()).isPresent())
			throw new UserException("UserName Already Exists.");

		user.setPassword(encoder.encode(user.getPassword()));
		repository.save(user);

		logger.warn("User registered: {}", user);
		return new SuccessResponse(true, "User SignedUp Successfully.", 201);
	}

	@Override
	public SuccessResponse verifyUserName(String userName) {
		Optional<UserDetails> userNameVerficication = repository.findByUserName(userName);
		logger.warn("User checker userName for {}", userName);
		if (userNameVerficication.isEmpty())
			return new SuccessResponse(true, "No User Present in the name " + userName, 200);

		return new SuccessResponse(false, "UserName Already exists.", 400);
	}

	@Override
	public SuccessResponse otpValidation(UserOTPDTO user) {
		UserDetails dto = repository.findByEmail(user.getEmail())
				.orElseThrow(() -> new UserException("User Not Found"));

		UserOTP otpDto = otpRepository.findByUser(dto)
				.orElseThrow(() -> new UserException("OTP not generated for this user."));

		long tenMinutes = 10 * 60 * 1000;
		long timeDifference = System.currentTimeMillis() - otpDto.getIntializedTime().getTime();

		if (timeDifference < tenMinutes)
			throw new DateTimeException("Otp has been expired.");

		if (!otpDto.getOtp().equals(user.getOtp()))
			throw new InvalidDataException("Invalid Otp.");

		otpRepository.delete(otpDto);
		logger.warn("User entered OTP for {}", user);
		return new SuccessResponse(true, "Otp Validated Successfully.", 200);

	}

	@Override
	public SuccessResponse forgetPassword(String email) {

		int otp = (int) (Math.random() * 900000) + 100000; // generates 6-digit OTP

		UserDetails dto = repository.findByEmail(email).orElseThrow(() -> new UserException("User not Found."));

		UserOTP userOtp = otpRepository.findByUser(dto).orElseGet(() -> new UserOTP());

		userOtp.setUser(dto);
		userOtp.setOtp(otp);
		userOtp.setIntializedTime(new Date());

		otpRepository.saveAndFlush(userOtp);

		sendEmail(email, otp);
		logger.warn("Rest pasword for {}", email);
		return new SuccessResponse(true, "Otp send Successfully to " + email, 200);
	}

	@Override
	public SuccessDataResponse getUserByID(Integer id) {
		UserDetails user = repository.findById(id).orElseThrow(() -> new UserException("User Not Found."));
		UserDetails mappedData = mapper.map(user, UserDetails.class);
		mappedData.setPassword(null);
		logger.warn("get User By id for {}", mappedData);
		return new SuccessDataResponse(true, "User Fetched Successfully.", 200, List.of(mappedData));
	}

	@Override
	public SuccessResponse updatedUser(UserDetails user, Integer id) {

		return null;
	}

	private void sendEmail(String toEmail, int otp) {
		try {
			MimeMessage mimeMessage = mailSender.createMimeMessage();

			MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

			String emailSubject = "Reset Password OTP";
			String emailContent = emailTemplate(otp);

			helper.setFrom(senderEmail);
			helper.setTo(toEmail);
			helper.setSubject(emailSubject);
			helper.setText(emailContent, true);
			mailSender.send(mimeMessage);
		} catch (MessagingException e) {
			System.err.println(e.getMessage());
			throw new RuntimeException("Something Went wrong.");
		}
	}

	private String emailTemplate(int otp) {

		return """
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
	}

}
