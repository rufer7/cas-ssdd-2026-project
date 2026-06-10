package ch.ssdd.eventhub.security.configuration;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Only active when local spring profile is active. These are dummy data which are loaded in memory.
 * The whole implementation is temporary till next lab with proper authentication is introduced.
 *
 * IMPORTANT: DO NOT USE THAT IN PRODUCTION - for convenience reasons only during local development
 */
@Profile("local")
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class InMemorySecurityConfiguration {

    @Value("${local.admin.username}")
    private String adminUsername;

    @Value("${local.admin.password}")
    private String adminPassword;

    @Value("${local.admin.role}")
    private String adminRole;

    @Value("${local.user.username}")
    private String username;

    @Value("${local.user.password}")
    private String userPassword;

    @Value("${local.user.role}")
    private String userRole;

    @Bean
    public InMemoryUserDetailsManager userDetailsService() {
        UserDetails admin = User.withUsername(adminUsername)
                .password("{noop}" + adminPassword)
                .roles(adminRole)
                .build();
        UserDetails user = User.withUsername(username)
                .password("{noop}" + userPassword)
                .roles(userRole)
                .build();
        return new InMemoryUserDetailsManager(admin, user);
    }


    @Bean
    public SecurityFilterChain localSecurityFilterChain(HttpSecurity http) {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                .httpBasic(Customizer.withDefaults());
        return http.build();
    }
}
