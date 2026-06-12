package ch.ssdd.eventhub.security.config;

import com.auth0.spring.boot.Auth0AuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy;
import org.springframework.security.web.header.writers.StaticHeadersWriter;

@Profile("!local")
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private static final String PERMISSIONS_POLICY = "accelerometer=(), autoplay=(), camera=(), display-capture=(), encrypted-media=(), fullscreen=(), geolocation=(), gyroscope=(), magnetometer=(), microphone=(), midi=(), payment=(), picture-in-picture=(), publickey-credentials-get=(), screen-wake-lock=(), sync-xhr=(), usb=(), web-share=(), xr-spatial-tracking=()";
    private static final String CSP_DIRECTIVES = "object-src 'none'; block-all-mixed-content; img-src 'none'; form-action 'none'; font-src 'none'; style-src 'none'; script-src 'none'; base-uri 'self'; frame-ancestors 'none'; require-trusted-types-for 'script'";

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, Auth0AuthenticationFilter authFilter) throws Exception {
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
                        .requestMatchers("/index.html").permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(authFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
