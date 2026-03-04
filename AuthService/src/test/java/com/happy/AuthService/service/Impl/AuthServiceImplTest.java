package com.happy.AuthService.service.Impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.happy.AuthService.common.response.ApiResponse;
import com.happy.AuthService.config.RsaKeyProperties;
import com.happy.AuthService.dto.AuthResponse;
import com.happy.AuthService.dto.LoginRequest;
import com.happy.AuthService.dto.RegisterRequest;
import com.happy.AuthService.entity.User;
import com.happy.AuthService.exception.BusinessException;
import com.happy.AuthService.repository.UserRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtEncoder jwtEncoder;
    @Mock
    private JwtDecoder jwtDecoder;
    @Mock
    private RsaKeyProperties rsaKeyProperties;
    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;

    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        authService = new AuthServiceImpl(
                userRepository,
                passwordEncoder,
                authenticationManager,
                jwtEncoder,
                jwtDecoder,
                rsaKeyProperties,
                redisTemplate);

        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void register_shouldSaveUserAndCachePassword_whenRequestIsValid() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("alice");
        request.setPassword("plain-pass");
        request.setEmail("alice@example.com");

        when(userRepository.existsByUsername("alice")).thenReturn(false);
        when(userRepository.existsByEmail("alice@example.com")).thenReturn(false);
        when(passwordEncoder.encode("plain-pass")).thenReturn("hashed-pass");

        ApiResponse response = authService.register(request);

        assertEquals("Register success", response.getMessage());
        verify(userRepository).save(any(User.class));
        verify(valueOperations).set(eq("auth:password:alice"), eq("hashed-pass"), eq(Duration.ofHours(24)));
    }

    @Test
    void register_shouldThrowBusinessException_whenUsernameExists() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("alice");
        request.setPassword("plain-pass");
        request.setEmail("alice@example.com");

        when(userRepository.existsByUsername("alice")).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class, () -> authService.register(request));

        assertEquals("Username already exists", ex.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_shouldAuthenticateAndReturnToken() {
        LoginRequest request = new LoginRequest();
        request.setUsername("alice");
        request.setPassword("plain-pass");

        User user = new User();
        user.setUsername("alice");
        user.setPassword("hashed-pass");
        user.setEmail("alice@example.com");
        user.setRoles("USER,ADMIN");

        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(rsaKeyProperties.getAccessTokenTtl()).thenReturn(3600L);

        Jwt encodedJwt = Jwt.withTokenValue("jwt-token")
                .header("alg", "RS256")
                .claim("sub", "alice")
                .build();
        when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenReturn(encodedJwt);

        AuthResponse response = authService.login(request);

        assertEquals("jwt-token", response.getToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals(3600L, response.getExpiresIn());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(valueOperations).set(eq("auth:password:alice"), eq("hashed-pass"), eq(Duration.ofHours(24)));
    }

    @Test
    void logout_shouldBlacklistJtiWithRemainingTtl_whenBearerTokenIsValid() {
        Instant expiresAt = Instant.now().plusSeconds(120);
        Jwt decodedJwt = Jwt.withTokenValue("jwt-token")
                .header("alg", "RS256")
            .claim("jti", "jti-123")
                .expiresAt(expiresAt)
                .claim("sub", "alice")
                .build();
        when(jwtDecoder.decode("jwt-token")).thenReturn(decodedJwt);

        ApiResponse response = authService.logout("Bearer jwt-token");

        assertEquals("Logout success. Please remove token from client storage", response.getMessage());

        ArgumentCaptor<Duration> ttlCaptor = ArgumentCaptor.forClass(Duration.class);
        verify(valueOperations).set(eq("auth:blacklist:jti-123"), eq("1"), ttlCaptor.capture());
        Duration ttl = ttlCaptor.getValue();
        assertFalse(ttl.isNegative());
        assertFalse(ttl.isZero());
        verify(jwtDecoder).decode("jwt-token");
    }
}
