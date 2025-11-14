package com.example.forgetPassword.config.jwtFilter;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Service;

import com.example.forgetPassword.entity.UserDetails;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

	private static final String SECRET_KEY = "HpUZXk2c359EwXSOgM8A6Etwthndqv63NttVRVG6s4k="; // Replace with your actual
																								// secret key

	public String generateToken(UserDetails userDetails) {

		Map<String, Object> claims = new HashMap<>();
		claims.put("email", userDetails.getEmail());
		claims.put("name", userDetails.getUserName());

		return Jwts.builder().claims().add(claims).subject("" + userDetails.getId())
				.issuedAt(new Date(System.currentTimeMillis()))
				.expiration(new Date(System.currentTimeMillis() + (30 * 60 * 1000))).and().signWith(getKey()).compact();

//		return "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IlNyaWRoYXJhbjFAZ21haWwuY29tIiwiaWF0IjoxNTE2MjM5MDIyfQ.Fj8mIEMN_xZzI30XOI29ZFI7oH71to8rcgquo0qRvwU";
	}

	private SecretKey getKey() {

		byte[] key = Decoders.BASE64.decode(SECRET_KEY);
		System.out.print("secretkey base64 decoded : " + Keys.hmacShaKeyFor(key).getAlgorithm());
		return Keys.hmacShaKeyFor(key);
	}

//	private String JwtKeyService() {
//		try {
//			
//			KeyGenerator keyGen = KeyGenerator.getInstance("HmacSHA256");
//			SecretKey sk = keyGen.generateKey();
//			return Base64.getEncoder().encodeToString(sk.getEncoded());
//		} catch (NoSuchAlgorithmException e) {
//			
//			e.printStackTrace();
//			return null;
//		}
//
//	}

	public String extractUsername(String token) {

		return extractClaim(token, Claims::getSubject);
	}

	private <T> T extractClaim(String token, Function<Claims, T> claimResolver) {
		final Claims claims = extractAllClaims(token);
		return claimResolver.apply(claims);
	}

	private Claims extractAllClaims(String token) {
		try {

			System.out.println("JWt servi9ce line 77 : "
					+ Jwts.parser().verifyWith(getKey()).build().parseSignedClaims(token).getPayload());
		} catch (Exception e) {
			if (e.getMessage().startsWith("JWT expired")) {
				System.err.println("\nJWT expired");
			} else {
				System.err.println(e);
			}
		}
		return Jwts.parser().verifyWith(getKey()).build().parseSignedClaims(token).getPayload();
	}

//	public String extractRole(String token) {
//		return extractClaim(token, Claims::get);
//	}

	private String extractEmail(String token) {
//		System.out.println("-------->"+extractClaim(token, t -> t.get("email")));
		return (String) extractClaim(token, t -> t.get("email"));
	}

	public boolean validateToken(String token, String name) {
		final String userName = extractEmail(token);
		System.out.println("JWT Service line 107 : " + "Token --->" + extractEmail(token));
		return userName.equals(name) && !isTokenExpired(token);
	}

	private boolean isTokenExpired(String token) {

		return extractExpiration(token).before(new Date());
	}

	private Date extractExpiration(String token) {

		return extractClaim(token, Claims::getExpiration);
	}

}
