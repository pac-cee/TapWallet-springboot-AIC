package rw.ac.auca.tapwallet.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
public class CurrentActorService {

    public Long idOr(Long fallback) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            Number claim = jwt.getClaim("userId");
            if (claim != null) return claim.longValue();
        }
        return fallback;
    }
}
