package ch.ssdd.eventhub.security.config;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Maps the roles carried in an Auth0 access token to Spring Security authorities.
 *
 * <p>Auth0 does not include application roles in tokens by default. Roles are added through a
 * <a href="https://auth0.com/docs/manage-users/access-control/sample-use-cases-actions-with-authorization#add-user-roles-to-tokens">Login Action</a>
 * under a namespaced custom claim (e.g. {@code https://eventhub.ssdd.ch/roles}) holding a JSON
 * array such as {@code ["Admin"]} or {@code ["User"]}. Each entry is mapped one-to-one to a
 * {@link SimpleGrantedAuthority} without a {@code ROLE_} prefix, matching the
 * {@code hasAuthority('Admin')} / {@code hasAnyAuthority('Admin','User')} expressions used in the
 * REST controllers.
 */
public class Auth0RolesAuthoritiesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    private final String rolesClaim;

    public Auth0RolesAuthoritiesConverter(String rolesClaim) {
        this.rolesClaim = Objects.requireNonNull(rolesClaim, "rolesClaim cannot be null");
    }

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        Object claim = jwt.getClaim(rolesClaim);
        if (!(claim instanceof Collection<?> roles)) {
            return List.of();
        }
        return roles.stream()
                .filter(Objects::nonNull)
                .map(Object::toString)
                .map(String::trim)
                .filter(role -> !role.isEmpty())
                .distinct()
                .<GrantedAuthority>map(SimpleGrantedAuthority::new)
                .toList();
    }
}
