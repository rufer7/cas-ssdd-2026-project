package ch.ssdd.eventhub.security.configuration;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

/**
 * Only active when local spring profile is active. These are dummy data which are loaded in memory
 */
@Profile("local")
@Configuration
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
        UserDetails user = User.withUsername(adminUsername)
                .password("{noop}" + adminPassword)
                .roles(adminRole)
                .build();
        UserDetails admin = User.withUsername(username)
                .password("{noop}" + userPassword)
                .roles(userRole)
                .build();
        return new InMemoryUserDetailsManager(user, admin);
    }
}
