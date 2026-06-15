package ch.ssdd.eventhub.security.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy;
import org.springframework.security.web.header.writers.StaticHeadersWriter;

/**
 * Security configuration for every profile except {@code local}. The backend acts as a stateless
 * OAuth2 resource server that validates the bearer access tokens issued by Auth0 on each request.
 *
 * <p>Tokens are verified against the Auth0 tenant (signature, issuer and audience) and the roles
 * carried in a namespaced custom claim are mapped to Spring Security authorities by
 * {@link Auth0RolesAuthoritiesConverter}. Endpoint authorization is then enforced declaratively via
 * {@code @PreAuthorize} on the REST controllers ({@link #ADMIN_AUTHORITY} / {@link #USER_AUTHORITY}).
 *
 * <p>Local development uses {@link InMemorySecurityConfiguration} instead and does not require Auth0.
 */
@Profile("!local")
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    /** Authority granted to administrators. Must match the value emitted in the Auth0 roles claim. */
    public static final String ADMIN_AUTHORITY = "Admin";

    /** Authority granted to regular users. Must match the value emitted in the Auth0 roles claim. */
    public static final String USER_AUTHORITY = "User";

    private static final String PERMISSIONS_POLICY = "accelerometer=(), autoplay=(), camera=(), display-capture=(), encrypted-media=(), fullscreen=(), geolocation=(), gyroscope=(), magnetometer=(), microphone=(), midi=(), payment=(), picture-in-picture=(), publickey-credentials-get=(), screen-wake-lock=(), sync-xhr=(), usb=(), web-share=(), xr-spatial-tracking=()";
    // Content Security Policy (CSP) for serving the bundled single-page app plus the API.
    // Scripts, styles and fonts are only allowed to be loaded from the application's origin.
    // Data URIs and https allowed as image source (e.g. Auth0 profile pictures).
    // connect-src allows the application's origin and https (Auth0 token endpoint).
    // No inline scripts allowed.
    private static final String CSP_DIRECTIVES = "default-src 'none'; script-src 'self'; style-src 'self'; img-src 'self' data: https:; font-src 'self'; connect-src 'self' https:; base-uri 'self'; form-action 'self'; frame-ancestors 'none'; object-src 'none'; worker-src 'self' blob:;";

    private final String issuerUri;
    private final String audience;
    private final String rolesClaim;

    public SecurityConfig(
            @Value("${auth0.issuer-uri}") String issuerUri,
            @Value("${auth0.audience}") String audience,
            @Value("${auth0.roles-claim}") String rolesClaim) {
        this.issuerUri = issuerUri;
        this.audience = audience;
        this.rolesClaim = rolesClaim;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http, JwtAuthenticationConverter jwtAuthenticationConverter) {
        http
                // Stateless resource server: clients authenticate on every request with a
                // bearer token in the Authorization header.
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(AbstractHttpConfigurer::disable)
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
                        // Static single-page app assets are public; the API stays authenticated.
                        .requestMatchers(HttpMethod.GET,
                                "/", "/index.html", "/assets/**", "/favicon.ico", "/*.svg")
                        .permitAll()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)));
        return http.build();
    }

    /**
     * Decoder that fetches the Auth0 JWK set lazily on first use (so the application starts even
     * when the tenant is briefly unreachable) and validates signature, issuer and audience. Auth0
     * exposes its keys at {@code <issuer>/.well-known/jwks.json}.
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        NimbusJwtDecoder decoder = NimbusJwtDecoder
                .withJwkSetUri(issuerUri + ".well-known/jwks.json")
                .build();

        OAuth2TokenValidator<Jwt> audienceValidator = new JwtClaimValidator<>(JwtClaimNames.AUD,
                aud -> aud.contains(audience));
        OAuth2TokenValidator<Jwt> withAudience = new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefaultWithIssuer(issuerUri),
                audienceValidator);
        decoder.setJwtValidator(withAudience);
        return decoder;
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(new Auth0RolesAuthoritiesConverter(rolesClaim));
        return converter;
    }
}
