package rw.ac.auca.tapwallet.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.stereotype.Service;
import rw.ac.auca.tapwallet.domain.User;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
public class JwtService {

    private final JwtEncoder encoder;
    private final Duration lifetime;

    public JwtService(JwtEncoder encoder,
                      @Value("${tapwallet.security.token-lifetime:PT8H}") Duration lifetime) {
        this.encoder = encoder;
        this.lifetime = lifetime;
    }

    public IssuedToken issue(User user) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(lifetime);
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("tapwallet-api")
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .subject(user.getEmail())
                .audience(List.of("tapwallet-web"))
                .claim("userId", user.getId())
                .claim("name", user.getFullName())
                .claim("roles", List.of(user.getRole().name()))
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return new IssuedToken(encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue(),
                lifetime.toSeconds());
    }

    public record IssuedToken(String value, long expiresIn) {
    }
}
