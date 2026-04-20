package com.companyapp.backend.controller;

import com.companyapp.backend.config.JwtService;
import com.companyapp.backend.entity.CustomUserDetails;
import com.companyapp.backend.entity.User;
import com.companyapp.backend.repository.UserRepository;
import com.companyapp.backend.services.UserService;
import com.companyapp.backend.services.AuditLogService; // OPRAVEN NÁZEV
import com.companyapp.backend.services.dto.request.AuthRequestDto;
import com.companyapp.backend.services.dto.request.UserRegistrationDto;
import com.companyapp.backend.services.dto.response.AuthResponseDto;
import com.companyapp.backend.services.dto.response.UserProfileDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final UserService userService;
    private final AuditLogService auditLogService; // OPRAVEN NÁZEV

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody AuthRequestDto request, HttpServletRequest httpRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            User user = userRepository.findByEmailAndIsActiveTrue(request.getEmail())
                    .orElseThrow(() -> new UsernameNotFoundException("Uživatel nenalezen."));

            // FIX: Použití logAction s explicitním performerem (email)
            auditLogService.logAction(
                    "LOGIN_SUCCESS",
                    "Auth",
                    user.getId().toString(),
                    "Úspěšné přihlášení z IP: " + getClientIp(httpRequest),
                    user.getEmail() // Tady ručně předáváme email, aby tam nebylo "System"
            );

            String jwtToken = jwtService.generateToken(userDetails);
            ResponseCookie jwtCookie = ResponseCookie.from("jwt", jwtToken)
                    .httpOnly(true)
                    .secure(false)
                    .path("/")
                    .maxAge(Duration.ofDays(1))
                    .sameSite("Lax")
                    .build();

            Set<String> roles = user.getRoles().stream().map(Enum::name).collect(Collectors.toSet());

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                    .body(AuthResponseDto.builder()
                            .token(null)
                            .userId(user.getId())
                            .email(user.getEmail())
                            .roles(roles)
                            .build());

        } catch (BadCredentialsException e) {
            // FIX: Logování neúspěchu s emailem
            auditLogService.logAction(
                    "LOGIN_FAILED",
                    "Auth",
                    "N/A",
                    "Neúspěšný pokus (špatné heslo) z IP: " + getClientIp(httpRequest),
                    request.getEmail()
            );
            throw e;
        }
    }

    @GetMapping("/me")
    public ResponseEntity<AuthResponseDto> getCurrentUser(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        User user = userRepository.findByEmailAndIsActiveTrue(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("Uživatel nenalezen."));

        Set<String> roles = user.getRoles().stream().map(Enum::name).collect(Collectors.toSet());

        return ResponseEntity.ok(AuthResponseDto.builder()
                .token(null)
                .userId(user.getId())
                .email(user.getEmail())
                .roles(roles)
                .build());
    }

    @PostMapping("/register")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGEMENT')")
    public ResponseEntity<UserProfileDto> register(@Valid @RequestBody UserRegistrationDto request) {
        UserProfileDto createdUser = userService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        return (xfHeader == null || xfHeader.isEmpty()) ? request.getRemoteAddr() : xfHeader.split(",")[0];
    }
}