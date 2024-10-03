package com.beehat.security;

import com.beehat.entity.Employee;
import com.beehat.repository.EmployeeRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class AdminSecurity {

    @Autowired
    private EmployeeRepo employeeRepository;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/admin/**").authenticated()
                        .anyRequest().permitAll()
                )
                .formLogin(form -> form
                        .loginPage("/admin/login") // Đường dẫn đến trang đăng nhập tùy chỉnh
                        .permitAll() // Cho phép tất cả người dùng truy cập trang đăng nhập
                        .defaultSuccessUrl("/admin/index") // Đường dẫn sau khi đăng nhập thành công
                        .failureUrl("/admin/login?error=true") // Đường dẫn nếu đăng nhập thất bại
                )
                .logout(logout -> logout
                        .logoutUrl("/admin/logout") // URL để đăng xuất
                        .logoutSuccessUrl("/admin/login?logout=true") // Đường dẫn sau khi đăng xuất
                        .permitAll() // Cho phép tất cả người dùng truy cập trang đăng xuất
                );

        return http.build();
    }

    @Bean
    public AuthenticationManager authManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder =
                http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder.userDetailsService(userDetailsService()).passwordEncoder(passwordEncoder());
        return authenticationManagerBuilder.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            Employee employee = employeeRepository.findByUsername(username);
            System.out.println("user:"+username);
            System.out.printf("em"+employee.getUsername()+"pass"+employee.getPassword());
            if (employee != null) {
                return org.springframework.security.core.userdetails.User
                        .withUsername(employee.getUsername())
                        .password(employee.getPassword())
                        .roles("EMPLOYEE")
                        .build();
            } else {
                throw new UsernameNotFoundException("User not found");
            }
        };
    }
}
