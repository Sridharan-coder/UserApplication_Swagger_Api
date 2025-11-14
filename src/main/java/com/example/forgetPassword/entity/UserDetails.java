package com.example.forgetPassword.entity;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDetails {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;

	@Column(unique = true, nullable = false)
	private String email;

	@Column(unique = true, nullable = false)
	private String userName;

	@Column(nullable = false)
	@Hidden
	private String password;

	public UserDetails(String email, String userName, String password) {
		super();
		this.email = email;
		this.userName = userName;
		this.password = password;
	}

}
