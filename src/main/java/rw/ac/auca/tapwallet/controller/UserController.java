package rw.ac.auca.tapwallet.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import rw.ac.auca.tapwallet.domain.User;
import rw.ac.auca.tapwallet.dto.UserRequest;
import rw.ac.auca.tapwallet.exception.BusinessRuleException;
import rw.ac.auca.tapwallet.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public User create(@Valid @RequestBody UserRequest request) {
        return userService.create(request);
    }

    @GetMapping
    public List<User> findAll() {
        return userService.findAll();
    }

    @GetMapping("/{id}")
    public User findById(@PathVariable Long id) {
        return userService.findById(id);
    }

    @PutMapping("/{id}")
    public User update(@PathVariable Long id, @Valid @RequestBody UserRequest request) {
        return userService.update(id, request);
    }

    @PatchMapping("/{id}/freeze")
    public User freeze(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        preventSelfLockout(id, jwt, "freeze");
        return userService.freeze(id);
    }

    @PatchMapping("/{id}/activate")
    public User activate(@PathVariable Long id) {
        return userService.activate(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        preventSelfLockout(id, jwt, "delete");
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private void preventSelfLockout(Long targetId, Jwt jwt, String action) {
        if (jwt != null && targetId.equals(((Number) jwt.getClaim("userId")).longValue())) {
            throw new BusinessRuleException("You cannot " + action + " your own signed-in administrator account.");
        }
    }
}
