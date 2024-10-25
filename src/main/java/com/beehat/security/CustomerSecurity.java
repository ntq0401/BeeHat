package com.beehat.security;

import com.beehat.entity.Customer;
import com.beehat.repo.CustomerRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import java.util.List;

@Configuration
@EnableWebSecurity
public class CustomerSecurity {
    @Autowired
    CustomerRepo customerRepo;
    @Bean
    public PasswordEncoder customerPasswordEncoder(){
        return new BCryptPasswordEncoder();
    }
    @Bean
    public SecurityFilterChain customerSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests((requests) -> requests
                        .requestMatchers("/customer/login").authenticated() // Chỉ yêu cầu xác thực
                        .anyRequest().permitAll()
                )
                .formLogin((form) -> form
                        .loginPage("/customer/login")
                        .defaultSuccessUrl("/customer/index", true)
                        .permitAll()
                )
                .logout((logout) -> logout
                        .logoutUrl("/logout") // Specify the logout URL
                        .logoutSuccessUrl("/customer/index") // Redirect after logout
                        .invalidateHttpSession(true) // Invalidate session on logout
                        .deleteCookies("JSESSIONID") // Delete session cookies
                        .permitAll() // Allow everyone to access logout
                );

        return http.build();
    }

    @Bean
    public UserDetailsService customerUserDetailsService() {
        return username -> {
            Customer customer = customerRepo.findByUsername(username);

            if (customer!=null) {
                return new org.springframework.security.core.userdetails.User(
                        customer.getUsername(),
                        customer.getPassword(),
                        List.of() // Không có vai trò
                );
            } else {
                throw new UsernameNotFoundException("User not found");
            }
        };
    }
}

