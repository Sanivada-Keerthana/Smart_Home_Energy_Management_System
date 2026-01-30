package com.srems.srems.config;

import com.srems.srems.model.Role;
import com.srems.srems.model.User;
import com.srems.srems.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${app.admin.username}")
    private String adminUsername;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Override
    public void run(String... args) {

        System.out.println("🔥 ADMIN PASSWORD FROM CONFIG = [" + adminPassword + "]");

        User admin = userRepository.findByUsername(adminUsername).orElse(null);

        if (admin == null) {
            // ✅ CREATE ADMIN
            admin = new User();
            admin.setUsername(adminUsername);
            admin.setEmail(adminEmail);
            admin.setRole(Role.ADMIN);
            admin.setBlockId(0L);   // IMPORTANT (avoid null crash)
            admin.setApproved(true);
            admin.setActive(true);
            admin.setPassword(passwordEncoder.encode(adminPassword));

            userRepository.save(admin);
            System.out.println("✅ ADMIN CREATED");

        } else {
            // 🔄 UPDATE PASSWORD IF CONFIG CHANGED
            if (!passwordEncoder.matches(adminPassword, admin.getPassword())) {
                admin.setPassword(passwordEncoder.encode(adminPassword));
                userRepository.save(admin);
                System.out.println("🔄 ADMIN PASSWORD UPDATED");
            }
        }
    }
}