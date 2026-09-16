package vn.edu.crs.auth_service.service;

import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import vn.edu.crs.auth_service.dto.LoginRequestDTO;
import vn.edu.crs.auth_service.dto.LoginResponseDTO;
import vn.edu.crs.auth_service.entity.User;
import vn.edu.crs.auth_service.exception.InvalidCredentialsException;
import vn.edu.crs.auth_service.repository.UserRepository;
import vn.edu.crs.auth_service.security.JwtUtil;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtUtil jwtUtil;

    public LoginResponseDTO login(
            LoginRequestDTO dto
    ) {

        User user =
                userRepository
                        .findByUsername(
                                dto.getUsername()
                        )
                        .orElseThrow(
                                () -> new InvalidCredentialsException(
                                        "Sai username hoac password"
                                )
                        );

        if (!passwordEncoder.matches(
                dto.getPassword(),
                user.getPassword()
        )) {

            throw new InvalidCredentialsException(
                    "Sai username hoac password"
            );
        }

        String token =
                jwtUtil.generateToken(
                        user.getId(),
                        user.getUsername(),
                        user.getRole()
                );

        return new LoginResponseDTO(
                user.getId(),
                token,
                user.getUsername(),
                user.getRole()
        );
    }
}