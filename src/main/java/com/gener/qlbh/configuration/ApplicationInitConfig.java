package com.gener.qlbh.configuration;


import com.gener.qlbh.enums.Role;
import com.gener.qlbh.models.User;
import com.gener.qlbh.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class ApplicationInitConfig {

    final PasswordEncoder passwordEncoder;
    @Bean
    ApplicationRunner applicationRunner(UserRepository userRepository){
        return args -> {
            if(userRepository.findByUsername("admin").isEmpty()){
                HashSet<String> roles = new HashSet<>();
//                roles.add(Role.ADMIN.name());
                User user = User.builder()
                        .roles(roles)
                        .password(passwordEncoder.encode("123456"))
                        .username("admin")
                        .build();

                User newUser = userRepository.save(user);
//                log.info(newUser.getId());

                log.info("Account Admin Has Been Created");
            }
        };
    }
}
