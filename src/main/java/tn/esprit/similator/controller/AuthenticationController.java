package tn.esprit.similator.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import tn.esprit.similator.dtos.AuthenticationRequest;
import tn.esprit.similator.dtos.AuthenticationResponse;
import tn.esprit.similator.dtos.PasswordVerificationRequest;
import tn.esprit.similator.dtos.RegistrationRequest;
import tn.esprit.similator.entity.User;
import tn.esprit.similator.security.JwtService;
import tn.esprit.similator.security.UserDetailsServiceIml;
import tn.esprit.similator.service.AuthenticationService;
import tn.esprit.similator.service.IUserService;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication")
@CrossOrigin(origins = "*")
public class AuthenticationController {

    private final AuthenticationService service;
    JwtService jwtService;
    UserDetailsServiceIml userDetailsService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ResponseEntity<?> register (@RequestBody @Valid RegistrationRequest request) throws MessagingException {
        service.register(request);
        return ResponseEntity.accepted().build();
    }
    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(
        @RequestBody @Valid AuthenticationRequest request
    ) {
        return ResponseEntity.ok(service.authenticate(request));
    }
    @GetMapping("/activate-account")
    public void confirm( @RequestParam String token) throws MessagingException {
        service.activateAccount(token);
    }
}
