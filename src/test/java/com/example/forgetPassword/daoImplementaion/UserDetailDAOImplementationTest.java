package com.example.forgetPassword.daoImplementaion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.forgetPassword.Exception.CustomException.InvalidDataException;
import com.example.forgetPassword.Exception.CustomException.UserException;
import com.example.forgetPassword.config.jwtFilter.JwtService;
import com.example.forgetPassword.dto.UserOTPDTO;
import com.example.forgetPassword.entity.UserDetails;
import com.example.forgetPassword.entity.UserOTP;
import com.example.forgetPassword.response.SuccessDataResponse;
import com.example.forgetPassword.response.SuccessResponse;
import com.example.forgetPassword.respository.UserOTPRepository;
import com.example.forgetPassword.respository.UserRepository;

import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletResponse;

@ExtendWith(MockitoExtension.class)
class UserDetailDAOImplementationTest {

	@InjectMocks
	private UserDetailDAOImplementation service; // The class you are testing

	@Mock
	private AuthenticationManager authManager;
	@Mock
	private JwtService jwtService;
	@Mock
	private UserRepository repository;
	@Mock
	private UserOTPRepository otpRepository;
	@Mock
	private ModelMapper mapper;
	@Mock
	private HttpServletResponse response;

	@Mock
	private Authentication authentication;

	@Mock
	private JavaMailSender mailSender;

	@Mock
	private BCryptPasswordEncoder encoder;

	@Mock
	MimeMessage mimeMessage;

	@Mock
	MimeMessageHelper helper;

	private static UserDetails user;

	@BeforeEach
	public void init() {
		user = new UserDetails();
		String output = String.format("%s  :: UserDetailDAOImplementationTest : UserDetails Object intialized ",
				"" + new Date(System.currentTimeMillis()));
		System.out.println(output);
	}

	@Test
	void signInSuccessTest() {

		// Given
		String email = "test@gmail.com";
		String password = "test123";

		user.setEmail(email);
		user.setPassword("encodedPass");

		when(repository.findByEmail(email)).thenReturn(Optional.of(user));

		when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);

		when(authentication.isAuthenticated()).thenReturn(true);

		when(mapper.map(any(UserDetails.class), eq(UserDetails.class))).thenReturn(user);

		when(jwtService.generateToken(any(UserDetails.class))).thenReturn("jwt-token");

		// When
		SuccessDataResponse result = service.signIn(email, password, response);

		// Then
		assertTrue(result.getSuccess());
		assertEquals(200, result.getStatusCode());
	}

	@Test
	void signUpSuccessTest() {

		user.setEmail("test@gmail.com");
		user.setUserName("testUser");
		user.setPassword("1234");

		when(repository.findByEmail("test@gmail.com")).thenReturn(Optional.empty());

		when(repository.findByUserName("testUser")).thenReturn(Optional.empty());

		SuccessResponse response = service.signUp(user);

		assertTrue(response.getSuccess());
		assertEquals(201, response.getStatusCode());
		assertEquals("User SignedUp Successfully.", response.getMessage());

		verify(repository).save(any(UserDetails.class));
	}

	@Test
	void signUpFailsWhenEmailExistsTest() {

		user.setEmail("test@gmail.com");
		user.setUserName("testUser");

		when(repository.findByEmail("test@gmail.com")).thenReturn(Optional.of(new UserDetails()));

		assertThrows(UserException.class, () -> service.signUp(user));
	}

	@Test
	void signUpFailsWhenUserNameExistsTest() {

		UserDetails user = new UserDetails();
		user.setEmail("test@gmail.com");
		user.setUserName("testUser");

		when(repository.findByEmail("test@gmail.com")).thenReturn(Optional.empty());

		when(repository.findByUserName("testUser")).thenReturn(Optional.of(new UserDetails()));

		assertThrows(UserException.class, () -> service.signUp(user));
	}

	@Test
	void verifyUserNameSuccessTest() {
		String userName = "Sridharan";
		when(repository.findByUserName(userName)).thenReturn(Optional.empty());

		SuccessResponse response = service.verifyUserName(userName);
		assertTrue(response.getSuccess());
		assertEquals(200, response.getStatusCode());
		assertEquals("No User Present in the name Sridharan", response.getMessage());

	}

	@Test
	void verifyUserNameExistsTest() {
		String userName = "Sridharan";
		when(repository.findByUserName(userName)).thenReturn(Optional.of(user));

		SuccessResponse response = service.verifyUserName(userName);
		assertFalse(response.getSuccess());
		assertEquals(400, response.getStatusCode());
		assertEquals("UserName Already exists.", response.getMessage());

	}

	@Test
	void otpValidationSuccessTest() {
		UserOTPDTO userOTPDTO = new UserOTPDTO("sri@gmail.com", 123456);
		when(repository.findByEmail(userOTPDTO.getEmail())).thenReturn(Optional.of(user));

		UserOTP userOtp = new UserOTP();
		userOtp.setOtp(123456);
		userOtp.setUser(user);

		when(otpRepository.findByUser(user)).thenReturn(Optional.of(userOtp));

		SuccessResponse response = service.otpValidation(userOTPDTO);

		assertTrue(response.getSuccess());
		assertEquals(200, response.getStatusCode());
		assertEquals("Otp Validated Successfully.", response.getMessage());
	}

	@Test
	void otpValidationNoUserTest() {
		UserOTPDTO userOTPDTO = new UserOTPDTO("sri@gmail.com", 123456);
		when(repository.findByEmail(userOTPDTO.getEmail())).thenReturn(Optional.empty());

		assertThrows(UserException.class, () -> service.otpValidation(userOTPDTO));

	}

	@Test
	void otpValidationInvalidOtpTest() {
		UserOTPDTO userOTPDTO = new UserOTPDTO("sri@gmail.com", 123456);
		when(repository.findByEmail(userOTPDTO.getEmail())).thenReturn(Optional.of(user));

		UserOTP userOtp = new UserOTP();
		userOtp.setOtp(123856);
		userOtp.setUser(user);

		when(otpRepository.findByUser(user)).thenReturn(Optional.of(userOtp));

		assertThrows(InvalidDataException.class, () -> service.otpValidation(userOTPDTO));
	}

	@Test
	void getUserByIdSccessTest() {
		Integer id = 12;
		user.setId(id);
		when(repository.findById(id)).thenReturn(Optional.of(user));
		when(mapper.map(user, UserDetails.class)).thenReturn(user);

		SuccessDataResponse response = service.getUserByID(id);

		assertTrue(response.getSuccess());
		assertEquals(200, response.getStatusCode());
		assertEquals("User Fetched Successfully.", response.getMessage());
		assertEquals(id, response.getResult().getFirst().getId());
	}

	@Test
	void getUserByIdNoUserTest() {
		Integer id = 12;
		when(repository.findById(id)).thenReturn(Optional.empty());
		assertThrows(UserException.class, () -> service.getUserByID(id));
	}

	@Test
	void forgetPasswordSuccessTest() {
		String email = "Sri@Gmail.com";
		user.setEmail(email);

		ReflectionTestUtils.setField(service, "senderEmail", "testSender@gmail.com");

		when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
		when(repository.findByEmail(email)).thenReturn(Optional.of(user));

		UserOTP userOtp = new UserOTP();
		userOtp.setOtp(123456);
		userOtp.setUser(user);
		when(otpRepository.findByUser(user)).thenReturn(Optional.of(userOtp));

		when(otpRepository.saveAndFlush(any(UserOTP.class))).thenReturn(userOtp);

		SuccessResponse response = service.forgetPassword(email);

		assertTrue(response.getSuccess());
		assertEquals(200, response.getStatusCode());

		verify(mailSender, times(1)).send(mimeMessage);
		verify(repository, times(1)).findByEmail(email);
		verify(otpRepository, times(1)).saveAndFlush(any(UserOTP.class));

	}

	@Test
	void forgetPasswordNoUserTest() {
		String email = "Sri@Gmail.com";
		user.setEmail(email);

		ReflectionTestUtils.setField(service, "senderEmail", "testSender@gmail.com");

		when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
		when(repository.findByEmail(email)).thenReturn(Optional.empty());

		assertThrows(UserException.class, () -> service.forgetPassword(email));
	}
}