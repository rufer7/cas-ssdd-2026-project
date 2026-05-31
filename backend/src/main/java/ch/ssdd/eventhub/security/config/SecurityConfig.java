package ch.ssdd.eventhub.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration for the EventHub backend.
 *
 * <p>Authentication is delegated to Microsoft Entra ID (OIDC). The
 * {@code spring-cloud-azure-starter-active-directory} starter auto-configures a
 * {@code JwtDecoder} that validates bearer tokens against the configured Entra ID
 * tenant (using {@code spring.cloud.azure.active-directory.*} properties) and a
 * {@code JwtAuthenticationConverter} that maps the {@code roles} claim of an
 * access token to authorities prefixed with {@code APPROLE_} and the {@code scp}
 * claim to authorities prefixed with {@code SCOPE_}.
 *
 * <p>The expected behaviour is:
 * <ul>
 *     <li>requests without a bearer token return {@code 401};</li>
 *     <li>requests to admin endpoints with a token that does not carry the
 *     {@code Admin} app role return {@code 403};</li>
 *     <li>requests with a valid bearer token return {@code 200}.</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /** Authority granted by the Entra ID app role {@code Admin}. */
    public static final String ADMIN_AUTHORITY = "APPROLE_Admin";

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Stateless resource server: clients authenticate on every request with a bearer
                // token in the Authorization header. CSRF protection is intentionally disabled
                // because no session/cookie-based authentication is used, so there is no ambient
                // authority that an attacker could exploit via cross-site requests.
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
                .authorizeHttpRequests(auth -> auth
                        // Endpoints reserved for administrators require the Entra ID "Admin" app role.
                        .requestMatchers("/api/admin/**").hasAuthority(ADMIN_AUTHORITY)
                        // The H2 console is only enabled for local development.
                        .requestMatchers("/h2-console/**").permitAll()
                        // Every other API call requires a valid bearer token.
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().permitAll())
                // Use the JwtDecoder auto-configured by spring-cloud-azure-starter-active-directory
                // for the configured Entra ID tenant.
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));

        return http.build();
    }
}
