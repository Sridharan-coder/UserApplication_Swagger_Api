package com.example.forgetPassword.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class SwaggerConfig {

	@Value("${prod.server-url}")
	private String prodServerUrl;

	@Bean
	OpenAPI forgetPasswordApplicationConfig() {

		Server prodServer = new Server().url(prodServerUrl)
				.description("When Application is running in the public port.");
		Server localServer = new Server().url("http://localhost:8080").description("default local domain.");

		String title = "User Access API.";

		Contact contact = new Contact();
		contact.email("sridharan.r@mitrahsoft.com");
		contact.name("Sridharan R");

		License license = new License().name("MIT Licence").url("https://choosealicense.com/licenses/mit/");

		Info info = new Info().title(title).version("1.0.0").contact(contact).description(title)
				.description("The User Access API For Practice.").license(license);

		Components components = new Components().addSecuritySchemes("cookieAuth", new SecurityScheme()
				.type(SecurityScheme.Type.APIKEY).in(SecurityScheme.In.COOKIE)
				.name("No Need to apply manually however backend was checking from it's cookie, signIn to access protected Api's."));

		return new OpenAPI().components(components).info(info).servers(List.of(prodServer, localServer))
				.paths(new Paths());
	}
}
