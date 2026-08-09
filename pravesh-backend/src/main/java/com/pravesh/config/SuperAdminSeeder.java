package com.pravesh.config;

import com.pravesh.entity.User;
import com.pravesh.entity.enums.Role;
import com.pravesh.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SuperAdminSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(SuperAdminSeeder.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${pravesh.superadmin.email}")
    private String superAdminEmail;

    @Value("${pravesh.superadmin.password}")
    private String superAdminPassword;

    @Override
    public void run(String... args) {
        boolean exists = userRepository.findAll().stream()
                .anyMatch(u -> u.getRole() == Role.SUPER_ADMIN);

        if (!exists) {
            User superAdmin = User.builder()
                    .name("Platform Super Admin")
                    .email(superAdminEmail)
                    .phone("9000000000")
                    .passwordHash(passwordEncoder.encode(superAdminPassword))
                    .role(Role.SUPER_ADMIN)
                    .state("N/A")
                    .isActive(true)
                    .build();
            userRepository.save(superAdmin);
            log.info("Bootstrap SUPER_ADMIN account created: {}", superAdminEmail);
        }
    }
}