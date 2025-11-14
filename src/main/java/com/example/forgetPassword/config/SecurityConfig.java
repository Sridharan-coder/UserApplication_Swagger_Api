package com.example.forgetPassword.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.example.forgetPassword.config.jwtFilter.JwtFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	private static final String[] AUTH_WHITELIST= {
			"/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v1/signIn",
            "/v1/signUp",
            "/v1/forgetPassword",
            "/v1/otpValidation",
            "/v1/userName/**",
            "/" 
	};
	
	private CustomUserDetailService userDetailService;
	private JwtFilter jwtFilter;
	
	
	
//	 public SecurityConfig(JwtFilter jwtFilter) {
		 public SecurityConfig(CustomUserDetailService userDetailService,JwtFilter jwtFilter) {
		 this.userDetailService=userDetailService;
		 this.jwtFilter = jwtFilter;
	}


	 @Bean
	    CorsConfigurationSource corsConfigurationSource() {
	    	CorsConfiguration configuration = new CorsConfiguration();
	        configuration.setAllowedOrigins(List.of("*")); //  Set allowed frontend URLs
	        configuration.setAllowedHeaders(List.of("*")); //  Allow all headers
	        configuration.setAllowedMethods(List.of("*")); //  Allow necessary methods
	        configuration.setAllowCredentials(true); //  Required for cookies to work
	        configuration.setExposedHeaders(List.of("Set-Cookie"));
			
			UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
			source.registerCorsConfiguration("/**", configuration);
			return source;
		} 
	 
	 

	@Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		 return http.authorizeHttpRequests(authorize -> authorize
                 .requestMatchers(AUTH_WHITELIST).permitAll().anyRequest().authenticated())
                 .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                 .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                 .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                 .csrf(csrf -> csrf.disable()).build();
    }

	@Bean
	AuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailService);
		provider.setPasswordEncoder(new BCryptPasswordEncoder(12));
		System.out.println("line : 84 apllication configuration : "+provider.toString());
		return provider;
	}

	@Bean
	AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
		System.out.println("line : 90 apllication configuration : "+config.toString());
		return config.getAuthenticationManager();
	}

	 
	
}
