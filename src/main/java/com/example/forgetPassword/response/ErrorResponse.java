package com.example.forgetPassword.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ErrorResponse {
	private Boolean success;
	private String message;
	private int statusCode;
}
