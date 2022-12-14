package com.artefact.api.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.stereotype.Component;

import java.util.Date;


@Component
public class JwtUtil {
    //TODO transfer to configuration
    private final String secret = "secret";

    public String generateToken(Long userId) throws IllegalArgumentException, JWTCreationException {
        return JWT.create()
                .withSubject("User details")
                .withClaim("userId", userId)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(new Date().getTime() + (1000 * 60 * 60 * 24)))
                //TODO transfer to configuration
                .withIssuer("artifact")
                .sign(Algorithm.HMAC256(secret));
    }

    public Long validateTokenAndRetrieveSubject(String token) throws JWTVerificationException {
        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secret))
                .withSubject("User details")
                .withIssuer("artifact")
                .build();
        DecodedJWT jwt = verifier.verify(token);
        return jwt.getClaim("userId").asLong();
    }
}
