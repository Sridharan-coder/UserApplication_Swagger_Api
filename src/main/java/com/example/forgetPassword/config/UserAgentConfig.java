package com.example.forgetPassword.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import nl.basjes.parse.useragent.UserAgentAnalyzer;

@Configuration
public class UserAgentConfig {
	@Bean
	UserAgentAnalyzer userAgentAnalyzer() {
		return UserAgentAnalyzer.newBuilder().hideMatcherLoadStats().withCache(10000).build();
	}
}