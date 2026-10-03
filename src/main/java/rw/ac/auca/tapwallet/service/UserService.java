package rw.ac.auca.tapwallet.service;

import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import rw.ac.auca.tapwallet.domain.AccountStatus;
import rw.ac.auca.tapwallet.domain.User;
import rw.ac.auca.tapwallet.dto.UserRequest;
import rw.ac.auca.tapwallet.exception.BusinessRuleException;
import rw.ac.auca.tapwallet.exception.DuplicateResourceException;
import rw.ac.auca.tapwallet.exception.ResourceNotFoundException;
import rw.ac.auca.tapwallet.repository.MerchantRepository;
import rw.ac.auca.tapwallet.repository.UserRepository;
import rw.ac.auca.tapwallet.repository.WalletRepository;

import java.util.List;

/**
 * Business rules:
 *  - email and phone are unique across all users
 *  - a user who owns a wallet or operates a merchant cannot be deleted
 *  - a user's role cannot change while they still own a wallet or a shop
 */
@Service
@Transactional
public class UserService {

    private final UserRepository users;
    private final WalletRepository wallets;
    private final MerchantRepository merchants;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository users, WalletRepository wallets, MerchantRepository merchants,
                       PasswordEncoder passwordEncoder) {
        this.users = users;
        this.wallets = wallets;
        this.merchants = merchants;
        this.passwordEncoder = passwordEncoder;
    }

    public User create(UserRequest request) {
        String email = request.email().trim().toLowerCase();
        String phone = normalisePhone(request.phone());
        if (users.existsByEmailIgnoreCase(email)) {
            throw new DuplicateResourceException("Email " + email + " is already registered.");
        }
        if (users.existsByPhone(phone)) {
            throw new DuplicateResourceException("Phone " + phone + " is already registered.");
        }
        String rawPassword = request.password() == null || request.password().isBlank()
                ? "ChangeMe@123"
                : request.password();
        return users.save(new User(request.fullName().trim(), email, phone, request.role(),
                passwordEncoder.encode(rawPassword)));
    }

    @Transactional(readOnly = true)
    public List<User> findAll() {
        return users.findAll();
    }

    @Transactional(readOnly = true)
    public User findById(Long id) {
        return users.findById(id).orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    public User update(Long id, UserRequest request) {
        User user = findById(id);
        String email = request.email().trim().toLowerCase();
        String phone = normalisePhone(request.phone());
        if (users.existsByEmailIgnoreCaseAndIdNot(email, id)) {
            throw new DuplicateResourceException("Email " + email + " is already registered to another user.");
        }
        if (users.existsByPhoneAndIdNot(phone, id)) {
            throw new DuplicateResourceException("Phone " + phone + " is already registered to another user.");
        }
        if (user.getRole() != request.role() && (wallets.existsByOwnerId(id) || merchants.existsByOperatorId(id))) {
            throw new BusinessRuleException("Role cannot change while the user still owns a wallet or operates a shop.");
        }
        user.setFullName(request.fullName().trim());
        user.setEmail(email);
        user.setPhone(phone);
        user.setRole(request.role());
        if (request.password() != null && !request.password().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(request.password()));
        }
        return users.save(user);
    }

    public User freeze(Long id) {
        User user = findById(id);
        user.setStatus(AccountStatus.FROZEN);
        return users.save(user);
    }

    public User activate(Long id) {
        User user = findById(id);
        user.setStatus(AccountStatus.ACTIVE);
        return users.save(user);
    }

    public void delete(Long id) {
        User user = findById(id);
        if (wallets.existsByOwnerId(id)) {
            throw new BusinessRuleException("Delete the user's wallet before deleting the user.");
        }
        if (merchants.existsByOperatorId(id)) {
            throw new BusinessRuleException("Delete the user's merchant shop before deleting the user.");
        }
        users.delete(user);
    }

    /** Stores every phone in the +250XXXXXXXXX form so uniqueness is not fooled by formatting. */
    private String normalisePhone(String raw) {
        String digits = raw.replaceAll("[^0-9]", "");
        return "+" + digits;
    }
}
