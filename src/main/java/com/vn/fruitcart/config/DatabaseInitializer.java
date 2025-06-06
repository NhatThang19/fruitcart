// package com.vn.fruitcart.config;

// import java.util.Optional;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.CommandLineRunner;
// import org.springframework.security.crypto.password.PasswordEncoder;
// import org.springframework.stereotype.Component;

// import com.vn.fruitcart.entity.Role;
// import com.vn.fruitcart.entity.User;
// import com.vn.fruitcart.service.RoleService;
// import com.vn.fruitcart.service.UserService;

// import jakarta.transaction.Transactional;

// @Component
// public class DatabaseInitializer implements CommandLineRunner {

//     @Autowired
//     private RoleService roleService;

//     @Autowired
//     private UserService userService;

//     @Autowired
//     private PasswordEncoder passwordEncoder;

//     @Override
//     @Transactional
//     public void run(String... args) throws Exception {
//         System.out.println("--- Bắt đầu kiểm tra và khởi tạo dữ liệu mặc định ---");

//         Role adminRole = createOrGetRole("ROLE_ADMIN");
//         Role userRole = createOrGetRole("ROLE_USER");

//         createOrGetUser("Đây là",
//                 "admin",
//                 "admin@example.com",
//                 "123456",
//                 adminRole);

//         createOrGetUser(
//                 "Đây là",
//                 "user",
//                 "user@example.com",
//                 "123456",
//                 userRole);

//         System.out.println("--- Hoàn tất khởi tạo dữ liệu mặc định ---");
//     }

//     private Role createOrGetRole(String roleName) {
//         Role existingRole = roleService.getRoleByName(roleName);
//         if (existingRole.isPresent()) {
//             System.out.println("Role '" + roleName + "' đã tồn tại.");
//             return existingRole.get();
//         } else {
//             Role newRole = new Role(roleName);
//             roleService.save(newRole);
//             System.out.println("Tạo mới Role: '" + roleName + "'");
//             return newRole;
//         }
//     }

//     private User createOrGetUser(String firstName, String lastName, String email, String rawPassword, Role role) {
//         if (!userService.existsByEmail(email)) {
//             User newUser = new User();
//             newUser.setFirstName(firstName);
//             newUser.setLastName(lastName);
//             newUser.setEmail(email);
//             newUser.setPassword(passwordEncoder.encode(rawPassword));
//             newUser.setRole(role);
//             newUser.setAvatarUrl("/storage/default/avatar.jpg");
//             userService.save(newUser);
//             System.out.println("Tạo mới User: '" + email + "' với Role: '" + role.getName() + "'");
//             return newUser;
//         } else {
//             System.out.println("User '" + email + "' đã tồn tại.");
//             return null;
//         }
//     }
// }