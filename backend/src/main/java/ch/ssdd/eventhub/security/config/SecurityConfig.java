package ch.ssdd.eventhub.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy;
import org.springframework.security.web.header.writers.StaticHeadersWriter;

/**
 * Security configuration for the event-hub backend.
 *
 * <p>
 * Authentication is delegated to Microsoft Entra ID (OIDC). The
 * {@code spring-cloud-azure-starter-active-directory} starter auto-configures a
 * {@code JwtDecoder} that validates bearer tokens against the configured Entra
 * ID
 * tenant (using {@code spring.cloud.azure.active-directory.*} properties) and a
 * {@code JwtAuthenticationConverter} that maps the {@code roles} claim of an
 * access token to authorities prefixed with {@code APPROLE_} and the
 * {@code scp}
 * claim to authorities prefixed with {@code SCOPE_}.
 *
 * <p>
 * The expected behaviour is:
 * <ul>
 * <li>requests without a bearer token return {@code 401};</li>
 * <li>requests to admin endpoints with a token that does not carry the
 * {@code Admin} app role return {@code 403};</li>
 * <li>requests with a valid bearer token return {@code 200}.</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /** Authority granted by the Entra ID app role {@code Admin}. */
    public static final String ADMIN_AUTHORITY = "APPROLE_Admin";
    private final String permissionsPolicy = "accelerometer=(), autoplay=(), camera=(), display-capture=(), encrypted-media=(), fullscreen=(), geolocation=(), gyroscope=(), magnetometer=(), microphone=(), midi=(), payment=(), picture-in-picture=(), publickey-credentials-get=(), screen-wake-lock=(), sync-xhr=(), usb=(), web-share=(), xr-spatial-tracking=()";
    private final String cspDirectives = "object-src 'none'; block-all-mixed-content; img-src 'none'; form-action 'none'; font-src 'none'; style-src 'none'; script-src 'none'; base-uri 'self'; frame-ancestors 'none'; require-trusted-types-for 'script'";

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Stateless resource server: clients authenticate on every request with a
                // bearer
                // token in the Authorization header. CSRF protection is intentionally disabled
                // because no session/cookie-based authentication is used, so there is no
                // ambient
                // authority that an attacker could exploit via cross-site requests.
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
                .csrf(Customizer.withDefaults())
                .headers(headers -> headers
                        .httpStrictTransportSecurity((hsts) -> hsts
                                .includeSubDomains(true)
                                .preload(true)
                                .maxAgeInSeconds(31536000))
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::deny)
                        .contentTypeOptions(
                                (contentTypeOptions) -> contentTypeOptions.disable())
                        .referrerPolicy(referrer -> referrer.policy(
                                ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                        .addHeaderWriter(new StaticHeadersWriter(
                                "cross-origin-opener-policy", "same-origin"))
                        .addHeaderWriter(new StaticHeadersWriter(
                                "cross-origin-embedder-policy", "require-corp"))
                        .addHeaderWriter(new StaticHeadersWriter(
                                "cross-origin-resource-policy", "same-origin"))
                        .permissionsPolicyHeader((permissions) -> permissions
                                .policy(permissionsPolicy))
                        .contentSecurityPolicy((csp) -> csp
                                .policyDirectives(cspDirectives)))
                .authorizeHttpRequests(auth -> auth
                        // Endpoints reserved for administrators require the Entra ID "Admin"
                        // app role.
                        .requestMatchers("/api/admin/**").hasAuthority(ADMIN_AUTHORITY)
                        // The H2 console is only enabled for local development.
                        .requestMatchers("/h2-console/**").permitAll()
                        // Every other API call requires a valid bearer token.
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().permitAll())
                // Use the JwtDecoder auto-configured by
                // spring-cloud-azure-starter-active-directory
                // for the configured Entra ID tenant.
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));

        return http.build();
    }
}
