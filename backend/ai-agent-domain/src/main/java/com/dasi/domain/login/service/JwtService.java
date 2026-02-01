package com.dasi.domain.login.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.dasi.domain.login.model.JwtProperties;
import com.dasi.domain.login.model.LoginUser;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;

@Service
public class JwtService {

    @Resource
    private JwtProperties jwtProperties;

    private Algorithm algorithm() {
        return Algorithm.HMAC256(jwtProperties.getSecret());
    }

    public String generateToken(User user) {
        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException("用户信息缺失，无法签发 Token");
        }
        Date now = new Date();
        long expireMillis = (jwtProperties.getExpireSeconds() == null ? 0L : jwtProperties.getExpireSeconds()) * 1000;
        Date expireAt = new Date(now.getTime() + expireMillis);

        return JWT.create()
                .withIssuer(jwtProperties.getIssuer())
                .withIssuedAt(now)
                .withExpiresAt(expireAt)
                .withClaim("userId", user.getId())
                .withClaim("username", user.getUsername())
                .withClaim("role", user.getRole())
                .sign(algorithm());
    }

    public DecodedJWT verifyToken(String token) {
        if (!StringUtils.hasText(token)) {
            throw new JWTVerificationException("Token 缺失");
        }
        JWTVerifier verifier = JWT.require(algorithm())
                .withIssuer(jwtProperties.getIssuer())
                .build();
        return verifier.verify(token);
    }

    public LoginUser parseLoginUser(String token) {
        DecodedJWT jwt = verifyToken(token);
        return LoginUser.builder()
                .id(jwt.getClaim("userId").asLong())
                .username(jwt.getClaim("username").asString())
                .role(jwt.getClaim("role").asString())
                .build();
    }
}
