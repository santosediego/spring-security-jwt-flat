package com.santosediego.cobaia_test.config;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.santosediego.cobaia_test.entities.User;

@Service
public class TokenService {

    private final String secret;
    private final long duration;
    private final String issuer;

    public TokenService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.duration}") long duration,
            @Value("${jwt.issuer}") String issuer
    ) {
        this.secret = secret;
        this.duration = duration;
        this.issuer = issuer;
    }

    public String generateToken(User user) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);

            return JWT.create()
                    .withIssuer(issuer)
                    .withSubject(user.getUsername())
                    .withExpiresAt(generateExpiration())
                    .sign(algorithm);

        } catch (JWTCreationException e) {
            throw new RuntimeException("Error while generating token", e);
        }
    }

    public String validateToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);

            return JWT.require(algorithm)
                    .withIssuer(issuer)
                    .build()
                    .verify(token)
                    .getSubject();

        } catch (JWTVerificationException e) {
            return null;
        }
    }

    private Instant generateExpiration() {
        return Instant.now().plusSeconds(duration);
    }
}
