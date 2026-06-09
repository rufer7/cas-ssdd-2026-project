package ch.ssdd.eventhub.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
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
 * Authentication is delegated to Microsoft Entra ID (OIDC).
 * The {@code spring-cloud-azure-starter-active-directory} starter auto-configures
 * a {@code JwtDecoder} that validates bearer tokens against the configured Entra ID tenant
 * (using {@code spring.cloud.azure.active-directory.*} properties) and a
 * {@code JwtAuthenticationConverter} that maps the {@code roles} claim of an
 * access token to authorities and the {@code scp} claim to authorities prefixed with {@code SCOPE_}.
 *
 * <p>
 * The expected behavior is:
 * <ul>
 * <li>requests without a bearer token return {@code 401};</li>
 * <li>requests to admin endpoints with a token that does not carry the
 * {@code Admin} app role return {@code 403};</li>
 * <li>requests to user endpoints with a token that does not carry the
 * {@code User} app role return {@code 403};</li>
 * <li>requests to with a valid bearer token and the required role return {@code 200/201}.</li>
 * </ul>
 */
@Profile("!local")
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    /** Authority granted by the Entra ID app role {@code Admin}. */
    public static final String ADMIN_AUTHORITY = "Admin";
    /** Authority granted by the Entra ID app role {@code User}. */
    public static final String USER_AUTHORITY = "User";
    private static final String PERMISSIONS_POLICY = "accelerometer=(), autoplay=(), camera=(), display-capture=(), encrypted-media=(), fullscreen=(), geolocation=(), gyroscope=(), magnetometer=(), microphone=(), midi=(), payment=(), picture-in-picture=(), publickey-credentials-get=(), screen-wake-lock=(), sync-xhr=(), usb=(), web-share=(), xr-spatial-tracking=()";
    private static final String CSP_DIRECTIVES = "object-src 'none'; block-all-mixed-content; img-src 'none'; form-action 'none'; font-src 'none'; style-src 'none'; script-src 'none'; base-uri 'self'; frame-ancestors 'none'; require-trusted-types-for 'script'";

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Stateless resource server: clients authenticate on every request with a
                // bearer token in the Authorization header.
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(Customizer.withDefaults())
                .headers(headers -> headers
                        .httpStrictTransportSecurity(hsts -> hsts
                                .includeSubDomains(true)
                                .preload(true)
                                .maxAgeInSeconds(31536000))
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::deny)
                        .referrerPolicy(referrer -> referrer.policy(
                                ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                        .addHeaderWriter(new StaticHeadersWriter(
                                "cross-origin-opener-policy", "same-origin"))
                        .addHeaderWriter(new StaticHeadersWriter(
                                "cross-origin-embedder-policy", "require-corp"))
                        .addHeaderWriter(new StaticHeadersWriter(
                                "cross-origin-resource-policy", "same-origin"))
                        .permissionsPolicyHeader(permissions -> permissions
                                .policy(PERMISSIONS_POLICY))
                        .contentSecurityPolicy(csp -> csp
                                .policyDirectives(CSP_DIRECTIVES)))
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().authenticated())
                // Use the JwtDecoder auto-configured by
                // spring-cloud-azure-starter-active-directory
                // for the configured Entra ID tenant.
                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(Customizer.withDefaults()));

        return http.build();
    }
}
