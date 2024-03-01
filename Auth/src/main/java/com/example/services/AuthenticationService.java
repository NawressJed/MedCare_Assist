package com.example.services;

import com.example.dto.request.AuthenticationRequest;
import com.example.dto.response.AuthenticationResponse;
import com.example.config.JwtService;
import com.example.dto.DoctorDTO;
import com.example.dto.PatientDTO;
import com.example.entities.Doctor;
import com.example.entities.Patient;
import com.example.entities.Token;
import com.example.entities.UserEntity;
import com.example.enums.ERole;
import com.example.enums.TokenType;
import com.example.mapper.AutoDoctorMapper;
import com.example.mapper.AutoPatientMapper;
import com.example.mapper.AutoUserMapper;
import com.example.repositories.TokenRepository;
import com.example.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    @Autowired
    UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final TokenRepository tokenRepository;
    @Autowired
    AutoPatientMapper autoPatientMapper;
    @Autowired
    ConfirmationTokenService confirmationTokenService;
    @Autowired
    RefreshTokenService refreshTokenService;
    @Autowired
    AutoDoctorMapper autoDoctorMapper;
    @Autowired
    AutoUserMapper autoUserMapper;
    private final AuthenticationManager authenticationManager;
    @Autowired
    private final EmailSenderService emailSenderService;
    private final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);

    public AuthenticationResponse register(PatientDTO request) {
        Patient patient = autoPatientMapper.toEntity(request);
        if (repository.existsByEmail(patient.getEmail())){
            logger.error("ERROR: Email already in use!");
            return null;
        }else {
            patient.setRole(ERole.PATIENT);
            patient.setPassword(passwordEncoder.encode(patient.getPassword()));
            repository.save(patient);
            var jwtToken = jwtService.generateToken(autoUserMapper.toDto(patient));
            Token confirmToken = confirmationTokenService.generateConfirmToken(autoUserMapper.toDto(patient));

            try {
                SimpleMailMessage mailMessage = new SimpleMailMessage();
                mailMessage.setTo(patient.getEmail());
                mailMessage.setSubject("Complete Registration!");
                mailMessage.setFrom("t99est97@gmail.com");
                mailMessage.setText("To confirm your account, please click here: "
                        + "http://localhost:8080/api/auth/confirm-account?token=" + confirmToken.getToken());

                emailSenderService.sendEmail(mailMessage);
            } catch (MailException ex) {
                ex.printStackTrace();
            }

            return AuthenticationResponse.builder()
                    .accessToken(jwtToken)
                    .build();
        }
    }

    public AuthenticationResponse register(DoctorDTO request) {
        Doctor doctor = autoDoctorMapper.toEntity(request);
        if (repository.existsByEmail(doctor.getEmail())){
            logger.error("ERROR: Email already in use!");
            return null;
        }else {
            doctor.setRole(ERole.DOCTOR);
            doctor.setPassword(passwordEncoder.encode(doctor.getPassword()));
            repository.save(doctor);
            var jwtToken = jwtService.generateToken(autoUserMapper.toDto(doctor));
            Token confirmToken = confirmationTokenService.generateConfirmToken(autoUserMapper.toDto(doctor));

            try {
                SimpleMailMessage mailMessage = new SimpleMailMessage();
                mailMessage.setTo(doctor.getEmail());
                mailMessage.setSubject("Complete Registration!");
                mailMessage.setFrom("t99est97@gmail.com");
                mailMessage.setText("To confirm your account, please click here: "
                        + "http://localhost:8080/api/auth/confirm-account?token=" + confirmToken.getToken());

                emailSenderService.sendEmail(mailMessage);
            } catch (MailException ex) {
                ex.printStackTrace();
            }

            return AuthenticationResponse.builder()
                    .accessToken(jwtToken)
                    .build();
        }
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        UserEntity user = repository.findByEmail(request.getEmail());
        Token confirmToken = tokenRepository.findByUserAndTokenType(user, TokenType.CONFIRM_ACCOUNT);
        if (confirmToken != null){
            logger.error("ERROR: Account not confirmed!");
            return null;
        }else {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
            var jwtToken = jwtService.generateToken(autoUserMapper.toDto(user));
            var refreshToken = refreshTokenService.generateRefreshToken(autoUserMapper.toDto(user));

            return AuthenticationResponse.builder()
                    .accessToken(jwtToken)
                    .refreshToken(refreshToken.toString())
                    .build();
        }
    }
}
