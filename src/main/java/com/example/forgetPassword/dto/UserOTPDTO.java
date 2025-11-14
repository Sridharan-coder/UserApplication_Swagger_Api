package com.example.forgetPassword.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserOTPDTO {

	@NotNull
	private String email;

	@NotNull
	private Integer otp;
}
