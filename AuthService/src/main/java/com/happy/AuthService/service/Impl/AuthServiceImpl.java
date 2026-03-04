package com.happy.AuthService.service.Impl;

import com.happy.AuthService.config.RsaKeyProperties;
import com.happy.AuthService.common.response.ApiResponse;
import com.happy.AuthService.dto.AuthResponse;
import com.happy.AuthService.dto.LoginRequest;
import com.happy.AuthService.dto.RegisterRequest;
import com.happy.AuthService.entity.User;
import com.happy.AuthService.exception.BusinessException;
import com.happy.AuthService.repository.UserRepository;
import com.happy.AuthService.service.AuthService;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;
    private final RsaKeyProperties rsaKeyProperties;
    private final StringRedisTemplate redisTemplate;

    public AuthServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           AuthenticationManager authenticationManager,
                           JwtEncoder jwtEncoder,
                           JwtDecoder jwtDecoder,
                           RsaKeyProperties rsaKeyProperties,
                           StringRedisTemplate redisTemplate) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtEncoder = jwtEncoder;
        this.jwtDecoder = jwtDecoder;
        this.rsaKeyProperties = rsaKeyProperties;
        this.redisTemplate = redisTemplate;
    }

    @Override
    @Transactional
    public ApiResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        user.setPassword(encodedPassword);
        user.setEmail(request.getEmail());
        user.setRoles("USER");
        userRepository.save(user);

        cacheHashedPassword(user.getUsername(), encodedPassword);
        return new ApiResponse("Register success");
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BusinessException("User not found"));

        cacheHashedPassword(user.getUsername(), user.getPassword());

        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(rsaKeyProperties.getAccessTokenTtl());
        String jti = UUID.randomUUID().toString();
        List<String> roles = Arrays.stream(user.getRoles().split(","))
                .map(String::trim)
                .filter(role -> !role.isEmpty())
                .toList();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("venuego-auth-service")
                .issuedAt(now)
                .expiresAt(expiry)
                .subject(user.getUsername())
                .id(jti)
                .claim("email", user.getEmail())
                .claim("roles", roles)
                .build();

        String token = jwtEncoder.encode(
                JwtEncoderParameters.from(
                        org.springframework.security.oauth2.jwt.JwsHeader.with(SignatureAlgorithm.RS256).build(),
                        claims))
                .getTokenValue();

        return new AuthResponse(token, "Bearer", rsaKeyProperties.getAccessTokenTtl());
    }

    @Override
    public ApiResponse logout(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return new ApiResponse("Logout success. Please remove token from client storage");
        }

        String token = authorizationHeader.substring(7);
        Jwt jwt = jwtDecoder.decode(token);
        String jti = jwt.getId();
        Instant expiresAt = jwt.getExpiresAt();

        if (jti != null && !jti.isBlank() && expiresAt != null) {
            Duration ttl = Duration.between(Instant.now(), expiresAt);
            if (!ttl.isNegative() && !ttl.isZero()) {
                redisTemplate.opsForValue().set("auth:blacklist:" + jti, "1", ttl);
            }
        }

        return new ApiResponse("Logout success. Please remove token from client storage");
    }

    private void cacheHashedPassword(String username, String hashedPassword) {
        redisTemplate.opsForValue().set("auth:password:" + username, hashedPassword, Duration.ofHours(24));
    }
}
