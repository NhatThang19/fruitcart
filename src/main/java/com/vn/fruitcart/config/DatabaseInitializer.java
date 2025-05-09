package com.vn.fruitcart.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.vn.fruitcart.entity.Role;
import com.vn.fruitcart.entity.User;
import com.vn.fruitcart.repository.RoleRepository;
import com.vn.fruitcart.repository.UserRepository;

@Service
public class DatabaseInitializer implements CommandLineRunner {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public DatabaseInitializer(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Value("${admin.email}")
    private String adminEmail;

    @Value("${admin.password}")
    private String adminPassword;

    @Override
    public void run(String... args) throws Exception {
        System.out.println(">>> START INIT DATABASE");

        long countUsers = this.userRepository.count();
        boolean hasAdminRole = this.roleRepository.existsByName("ADMIN");
        boolean hasUserRole = this.roleRepository.existsByName("USER");

        System.out.println(">>> INIT DATABASE ~ CHECKING ROLES...");

        if (!hasAdminRole) {
            Role adminRole = new Role("ADMIN");
            this.roleRepository.save(adminRole);
            System.out.println(">>> CREATED ADMIN ROLE");
        }

        if (!hasUserRole) {
            Role userRole = new Role("USER");
            this.roleRepository.save(userRole);
            System.out.println(">>> CREATED USER ROLE");
        }

        if (hasAdminRole && hasUserRole) {
            System.out.println(">>> SKIP INIT DATABASE ~ ALREADY HAVE ALL REQUIRED ROLES");
        }

        if (countUsers == 0) {
            User adminUser = new User();
            adminUser.setUsername("admin");
            adminUser.setEmail(adminEmail);
            adminUser.setPassword(this.passwordEncoder.encode(adminPassword));
            adminUser.setRole(this.roleRepository.findRoleByName("ADMIN"));
            adminUser.setActive(true);
            adminUser.setAvatar("/storage/avatar.jpg");

            this.userRepository.save(adminUser);
        }

        if (countUsers > 0) {
            System.out.println(">>> SKIP INIT DATABASE ~ ALREADY HAVE DATA...");
        } else {
            System.out.println(">>> END INIT DATABASE");
        }
    }

}
