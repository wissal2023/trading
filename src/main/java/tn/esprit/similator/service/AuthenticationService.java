package tn.esprit.similator.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import tn.esprit.similator.dtos.AuthenticationRequest;
import tn.esprit.similator.dtos.AuthenticationResponse;
import tn.esprit.similator.dtos.RegistrationRequest;
import tn.esprit.similator.entity.*;
import tn.esprit.similator.repository.RoleRepository;
import tn.esprit.similator.repository.TokenRepository;
import tn.esprit.similator.repository.UserRepo;
import tn.esprit.similator.security.JwtService;

//package tn.esprit.similator.service;
@Service
@Slf4j
@RequiredArgsConstructor
public class AuthenticationService {
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserRepo userRepository;
    private final TokenRepository tokenRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final MailingService emailService;
    @Value("${application.mailing.frontend.activation-url}")
    private String activationUrl;
    public void register(RegistrationRequest request) throws MessagingException {
        try {
            log.info("Registering user: {}", request.getEmail());
            var userRole = roleRepository.findByName(UserRole.CUSTOMER.name())
                .orElseThrow(() -> new IllegalStateException("ROLE CUSTOMER was not initialized"));
       // Create a new portfolio for the user
        Portfolio portfolio = new Portfolio();
        portfolio.setTotVal(100000.00); // Initial total value for the portfolio
        portfolio.setDateCreated(new Date());
        // Create User object and link the portfolio
        var user = User.builder()
                .fullname(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .accountLocked(false)
                .enabled(false)
                .roles(List.of(userRole))
                .portfolio(portfolio) // Assign portfolio to the user
                .build();
        userRepository.save(user);// Save the user (this will also save the portfolio if cascade is set)
        sendValidationEmail(user); // Send validation email
        } catch (Exception e) {
            log.error("Registration failed: ", e);
            throw e; // Or handle it as needed
        }
    }
    private void sendValidationEmail(User user) throws MessagingException {
        log.info("Sending validation email to: " + user.getEmail());
        var newToken = generateAndSaveActivationToken(user);
        log.info("Generated activation token: " + newToken);
        emailService.sendEmail(
                user.getEmail(),
                user.getFullname(),
                EmailTemplateName.ACTIVATE_ACCOUNT,
                activationUrl,
                newToken,
                "Account activation");
    }
    private String generateAndSaveActivationToken(User user) {
        String generatedToken = generateActivationCode(6);
        var token = Token.builder()
                            .token(generatedToken)
                            .createdAt(LocalDateTime.now())
                            .expiredAt(LocalDateTime.now().plusMinutes(15))
                            .user(user)
                            .build();                
        tokenRepository.save(token);
        return generatedToken;
    }
    private String generateActivationCode(int length) {
        String characters = "0123456789";
        StringBuilder codeBuilder = new StringBuilder();
        SecureRandom secureRandom = new SecureRandom();
        for (int i = 0; i < length; i++) {
            int randomIndex = secureRandom.nextInt(characters.length()); // random index from 0 to 9
            codeBuilder.append(characters.charAt(randomIndex));
        }
        return codeBuilder.toString();
    }
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        log.debug("Attempting to authenticate user: {}", request.getEmail());
        try {
        var auth = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getEmail(),
                request.getPassword()
                ));
        var claims = new HashMap<String, Object>();
        var user = ((User)auth.getPrincipal());
        Long portfolioId = user.getPortfolio() != null ? user.getPortfolio().getId() : null;//new code
        claims.put("username", user.getUsername());
        claims.put("portfolioId", portfolioId); //new code
        var jwtToken = jwtService.generateToken(claims, user);
        return AuthenticationResponse.builder()
                                        .user(user)
                                        .token(jwtToken)
                                        .build();
        } catch (BadCredentialsException e) {
            log.error("Authentication failed for user: {}", request.getEmail(), e);
            throw new RuntimeException("Invalid credentials", e);
        }

    }

    public void activateAccount(String token) throws MessagingException {
        Token savedToken = tokenRepository.findByToken(token).orElseThrow(() -> new RuntimeException("Invalid token"));
        if(LocalDateTime.now().isAfter(savedToken.getExpiredAt())) {
            sendValidationEmail(savedToken.getUser());
            throw new RuntimeException("Activation token has expired. A new token has been sent to the same email adress.");
        }
        var user = userRepository.findById(savedToken.getUser().getId())
                                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        user.setEnabled(true);
        userRepository.save(user);
        savedToken.setValidatedAt(LocalDateTime.now());
        tokenRepository.save(savedToken);
    }
}

