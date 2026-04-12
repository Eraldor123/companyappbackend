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
import org.springframework.http.HttpStatus;
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
        // Autentizace přes Spring Security (toto automaticky porovná zahashované heslo)
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // Pokud to projde sem, znamená to, že email i heslo jsou 100% správné
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userRepository.findByEmailAndIsActiveTrue(request.getEmail()).orElseThrow();

        // Vygenerování tokenu
        String jwtToken = jwtService.generateToken(userDetails);

        return ResponseEntity.ok(AuthResponseDto.builder()
                .token(jwtToken)
                .userId(user.getId())
                .email(user.getEmail())
                // Převedeme Set<AccessLevel> na Set<String>
                .roles(user.getRoles().stream().map(Enum::name).collect(java.util.stream.Collectors.toSet()))
                .build());
    }

    // TADY JSME PŘIDALI VYHAZOVAČE: Zkontroluje, zda má uživatel z JWT tokenu potřebnou roli
    @PostMapping("/register")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGEMENT')")
    public ResponseEntity<UserProfileDto> register(@Valid @RequestBody UserRegistrationDto request) {
        UserProfileDto createdUser = userService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }
}