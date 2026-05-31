package ch.ssdd.eventhub.admin.controller;

import java.util.Map;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Administrative endpoints reserved for users carrying the Entra ID {@code Admin}
 * app role. Authorisation is enforced centrally in
 * {@link ch.ssdd.eventhub.security.config.SecurityConfig}.
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @GetMapping("/ping")
    public Map<String, String> ping(@AuthenticationPrincipal Jwt principal) {
        return Map.of(
                "status", "ok",
                "subject", principal.getSubject());
    }
}
