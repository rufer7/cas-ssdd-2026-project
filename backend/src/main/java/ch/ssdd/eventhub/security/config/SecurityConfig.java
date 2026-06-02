package ch.ssdd.eventhub.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy;
import org.springframework.security.web.header.writers.StaticHeadersWriter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final String permissionsPolicy = "accelerometer=(), autoplay=(), camera=(), display-capture=(), encrypted-media=(), fullscreen=(), geolocation=(), gyroscope=(), magnetometer=(), microphone=(), midi=(), payment=(), picture-in-picture=(), publickey-credentials-get=(), screen-wake-lock=(), sync-xhr=(), usb=(), web-share=(), xr-spatial-tracking=()";
    private final String cspDirectives = "object-src 'none'; block-all-mixed-content; img-src 'none'; form-action 'none'; font-src 'none'; style-src 'none'; script-src 'none'; base-uri 'self'; frame-ancestors 'none'; require-trusted-types-for 'script'";

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        http
                .csrf(Customizer.withDefaults())
                .headers(headers -> headers
                        .httpStrictTransportSecurity((hsts) -> hsts
                                .includeSubDomains(true)
                                .preload(true)
                                .maxAgeInSeconds(31536000))
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::deny)
                        .contentTypeOptions((contentTypeOptions) -> contentTypeOptions.disable())
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
                        .anyRequest().permitAll());

        return http.build();
    }
}