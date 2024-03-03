package ru.enzhine.phw2.backend.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import ru.enzhine.phw2.backend.services.UsersServiceImpl


@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class SecurityConfig {

    @Bean
    fun encryptor(): PasswordEncoder = BCryptPasswordEncoder()


    @Bean
    fun users(): UserDetailsService = UsersServiceImpl()

    @Bean
    @Throws(Exception::class)
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .cors { it.disable() }
            .csrf { it.disable() }
            .authorizeHttpRequests { it
                .requestMatchers("/login*", "/css/*").permitAll()
                .requestMatchers("*.html").denyAll()
                .requestMatchers("/admin**").hasRole("ADMIN")
                .requestMatchers("/user**").hasRole("CUSTOMER")
                .anyRequest().authenticated()
            }
            .formLogin { it
                .loginPage("/login")
                .failureUrl("/login?wrong")
                .defaultSuccessUrl("/menu", true)
            }
            .logout { it
                .logoutSuccessUrl("/index")
            }
            .exceptionHandling { it
                .accessDeniedPage("/403")
            }
            .userDetailsService(users())
        return http.build()
    }
}