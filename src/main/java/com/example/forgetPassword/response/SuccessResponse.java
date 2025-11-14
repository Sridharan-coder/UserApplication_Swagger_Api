package com.example.forgetPassword.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SuccessResponse {
	private Boolean success;
	private String message;
	private int statusCode;
}
