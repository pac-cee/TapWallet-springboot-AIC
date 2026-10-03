package rw.ac.auca.tapwallet.security;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import rw.ac.auca.tapwallet.domain.User;
import rw.ac.auca.tapwallet.dto.AuthResponse;
import rw.ac.auca.tapwallet.dto.LoginRequest;
import rw.ac.auca.tapwallet.repository.UserRepository;

@Service
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository users;
    private final JwtService jwtService;

    public AuthenticationService(AuthenticationManager authenticationManager, UserRepository users,
                                 JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.users = users;
        this.jwtService = jwtService;
    }

    public AuthResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase();
        authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(email, request.password()));
        User user = users.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalStateException("Authenticated user no longer exists."));
        JwtService.IssuedToken token = jwtService.issue(user);
        return new AuthResponse(token.value(), "Bearer", token.expiresIn(), user.getId(),
                user.getFullName(), user.getEmail(), user.getRole(), user.getStatus());
    }
}
