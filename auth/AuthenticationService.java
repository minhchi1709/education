package com.mchis.auth;

import com.mchis.email.EmailService;
import com.mchis.email.EmailTemplateName;
import com.mchis.role.RoleRepository;
import com.mchis.security.JwtService;
import com.mchis.user.Token;
import com.mchis.user.TokenRepository;
import com.mchis.user.User;
import com.mchis.user.UserRepository;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RoleRepository roleRepository;
    private final EmailService emailService;
    private final TokenRepository tokenRepository;

    @Value("${application.mailing.frontend.activation-url}")
    private String activationUrl;
    @Value("${application.mailing.frontend.enter-token-url}")
    private String enterTokenUrl;

    public void register(RegistrationRequest request) throws MessagingException {
        var userRole = roleRepository.findByName("USER")
                // todo - better exception handling
                .orElseThrow(() -> new IllegalStateException("ROLE USER was not initiated"));
        var user = User.builder()
                .firstname(request.getFirstname())
                .lastname(request.getLastname())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .createdDate(LocalDateTime.now())
                .accountLocked(false)
                .enabled(false)
                .roles(List.of(userRole))
                .learningCourses(new ArrayList<>())
                .teachingCourses(new ArrayList<>())
                .assistingCourses(new ArrayList<>())
                .grades(new ArrayList<>())
                .build();
        userRepository.save(user);
        sendValidationEmail(user);
    }

    public void sendValidationEmailToSetNewPassword(ForgotPasswordRequest request) throws MessagingException {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UsernameNotFoundException(String.format("User %s not found", request.email())));
        sendValidationEmailForForgotPassword(user);
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        var auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        var claims = new HashMap<String, Object>();
        var user = ((User) auth.getPrincipal());
        claims.put("fullName", user.getFullName());

        var jwtToken = jwtService.generateToken(claims, (User) auth.getPrincipal());
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }

    public String changePassword(ChangePasswordRequest request, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        if (passwordEncoder.matches(request.oldPassword(), user.getPassword())) {
            user.setPassword(passwordEncoder.encode(request.newPassword()));
            return "ok";
        }
        else {
            return "wrong password";
        }
    }

    public void resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UsernameNotFoundException(String.format("User %s not found", request.email())));
        user.setPassword(passwordEncoder.encode(request.password()));
        userRepository.save(user);
    }

    @Transactional
    public void activateAccount(String token) throws MessagingException {
        Token savedToken = tokenRepository.findByToken(token)
                // todo exception has to be defined
                .orElseThrow(() -> new RuntimeException("Invalid token"));
        if (LocalDateTime.now().isAfter(savedToken.getExpiresAt())) {
            sendValidationEmail(savedToken.getUser());
            throw new RuntimeException("Activation token has expired. A new token has been send to the same email address");
        }

        var user = userRepository.findById(savedToken.getUser().getId())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        user.setEnabled(true);
        userRepository.save(user);

        tokenRepository.delete(savedToken);
    }

    public String validateToken(String token) throws MessagingException {
        Optional<Token> savedTokenOptional = tokenRepository.findByToken(token);
        if (savedTokenOptional.isEmpty()) {
            return enterTokenUrl;
        }
        Token savedToken = savedTokenOptional.get();
        if (LocalDateTime.now().isAfter(savedToken.getExpiresAt())) {
            sendValidationEmailForForgotPassword(savedToken.getUser());
            return "The code is not correct, please try again";
        }
        return savedToken.getUser().getEmail();
    }

    private void sendValidationEmailForForgotPassword(User user) throws MessagingException {
        var newToken = generateAndSaveActivationToken(user);

        emailService.sendEmail(
                user.getEmail(),
                user.getFullName(),
                EmailTemplateName.RESET_PASSWORD,
                activationUrl,
                newToken,
                "Reset Password"
        );
    }

    private void sendValidationEmail(User user) throws MessagingException {
        var newToken = generateAndSaveActivationToken(user);

        emailService.sendEmail(
                user.getEmail(),
                user.getFullName(),
                EmailTemplateName.ACTIVATE_ACCOUNT,
                activationUrl,
                newToken,
                "Account activation"
        );
    }

    private String generateAndSaveActivationToken(User user) {
        // Generate a token
        String generatedToken = generateActivationCode(6);
        // if there is already a token with token = generatedToken in the database
        // create new token to ensure each token is mapped to exactly one user
        while(tokenRepository.findByToken(generatedToken).isPresent()) {
            generatedToken = generateActivationCode(6);
        }
        var token = Token.builder()
                .token(generatedToken)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(15))
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
            int randomIndex = secureRandom.nextInt(characters.length());
            codeBuilder.append(characters.charAt(randomIndex));
        }

        return codeBuilder.toString();
    }
}
