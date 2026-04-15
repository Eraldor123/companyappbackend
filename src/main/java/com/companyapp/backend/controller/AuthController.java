package com.companyapp.backend.controller;

import com.companyapp.backend.config.JwtService;
import com.companyapp.backend.entity.CustomUserDetails;
import com.companyapp.backend.entity.User;
import com.companyapp.backend.repository.UserRepository;
import com.companyapp.backend.services.UserService;
import com.companyapp.backend.services.dto.request.AuthRequestDto;
import com.companyapp.backend.services.dto.request.UserRegistrationDto;
import com.companyapp.backend.services.dto.response.AuthResponseDto;
import com.companyapp.backend.services.dto.response.UserProfileDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody AuthRequestDto request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userRepository.findByEmailAndIsActiveTrue(request.getEmail()).orElseThrow();

        String jwtToken = jwtService.generateToken(userDetails);

        // --- VYTVOŘENÍ BEZPEČNÉ HTTP-ONLY COOKIE ---
        ResponseCookie jwtCookie = ResponseCookie.from("jwt", jwtToken)
                .httpOnly(true)
                .secure(false)
                .path("/")
                // OPRAVENO: Přidáno 'L' k prvnímu číslu, aby se výpočet prováděl v typu long
                .maxAge(24L * 60 * 60)
                .sameSite("Lax")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .body(AuthResponseDto.builder()
                        .token(jwtToken)
                        .userId(user.getId())
                        .email(user.getEmail())
                        .roles(user.getRoles().stream().map(Enum::name).collect(java.util.stream.Collectors.toSet()))
                        .build());
    }

    @PostMapping("/register")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGEMENT')")
    public ResponseEntity<UserProfileDto> register(@Valid @RequestBody UserRegistrationDto request) {
        UserProfileDto createdUser = userService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }
}