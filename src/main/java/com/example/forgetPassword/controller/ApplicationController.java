package com.example.forgetPassword.controller;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.forgetPassword.Exception.CustomException.UserException;
import com.example.forgetPassword.response.SuccessResponse;

import eu.bitwalker.useragentutils.UserAgent;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import nl.basjes.parse.useragent.UserAgent.ImmutableUserAgent;
import nl.basjes.parse.useragent.UserAgentAnalyzer;

@RestController
@RequestMapping("/")
@Tag(name = "Api Check", description = "To check the application is working.")
public class ApplicationController {

	private final UserAgentAnalyzer uaa;

	public ApplicationController(UserAgentAnalyzer uaa) {
		this.uaa = uaa;
	}

	@Operation(summary = "get API Working message.", description = "Verify it with the API is Working.")
	@GetMapping(produces = "application/json")
	public ResponseEntity<SuccessResponse> apiCheck(HttpServletRequest request) throws UnknownHostException {
		String clientIp = getClientIp(request);
		String deviceInfo = getClientDeviceInfo(request);

		UserAgent userAgent = UserAgent.parseUserAgentString(request.getHeader("User-Agent"));
		String browser = userAgent.getBrowser().getName();
		String os = userAgent.getOperatingSystem().getName();
		
		String headerModel = request.getHeader("X-Device-Model");
		String headerName = request.getHeader("X-Device-Name");
		if (headerModel != null || headerName != null) {
			throw new UserException("Model(header): " + headerModel + ", Name(header): " + headerName);
		}

		
		String ua = request.getHeader("User-Agent");
		if (ua == null) {
			throw new UserException("No user-agent present");
		}

		ImmutableUserAgent agent = uaa.parse(ua);

		String deviceClass = agent.getValue("DeviceClass"); // Mobile / Desktop / Tablet / Bot
		String deviceName = agent.getValue("DeviceName"); // "Galaxy S21"
		String deviceBrand = agent.getValue("DeviceBrand"); // "Samsung"
		String deviceCpu = agent.getValue("DeviceCpu"); // cpu info (sometimes)

			InetAddress address=InetAddress.getLocalHost();
			String ipAdr=address.getHostAddress();
			String hostName=address.getHostName();
		

		String message = String.format(
				"API is working. Request received from  EthernetIp:%s,   systemName:%s,   IP: %s,   Device: %s,   class=%s,   brand=%s,   name=%s,   cpu=%s,   Browser:%s,   Os:%s",
				ipAdr,hostName,clientIp, deviceInfo, deviceClass, deviceBrand, deviceName, deviceCpu,browser,os);
		SuccessResponse successResponse = new SuccessResponse(true, message, 200);
		return ResponseEntity.ok(successResponse);
	}

	private String getClientIp(HttpServletRequest request) {
		String ip = request.getHeader("X-Forwarded-For");
		if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
		}
		if (ip.contains(",")) {
			ip = ip.split(",")[0];
		}
		return ip;
	}

	private String getClientDeviceInfo(HttpServletRequest request) {
		return request.getHeader("User-Agent");
	}
}
