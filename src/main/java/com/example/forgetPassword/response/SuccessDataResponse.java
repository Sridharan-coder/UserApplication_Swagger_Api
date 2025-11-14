package com.example.forgetPassword.response;

import java.util.List;

import com.example.forgetPassword.entity.UserDetails;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SuccessDataResponse {
	private Boolean success;
	private String message;
	private int statusCode;
	private List<UserDetails> result;
}
