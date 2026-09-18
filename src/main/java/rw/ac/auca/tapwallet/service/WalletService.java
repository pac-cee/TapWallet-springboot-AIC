package rw.ac.auca.tapwallet.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.ac.auca.tapwallet.domain.Role;
import rw.ac.auca.tapwallet.domain.User;
import rw.ac.auca.tapwallet.domain.Wallet;
import rw.ac.auca.tapwallet.dto.WalletRequest;
import rw.ac.auca.tapwallet.dto.WalletUpdateRequest;
import rw.ac.auca.tapwallet.exception.BusinessRuleException;
import rw.ac.auca.tapwallet.exception.DuplicateResourceException;
import rw.ac.auca.tapwallet.exception.ResourceNotFoundException;
import rw.ac.auca.tapwallet.repository.PaymentRepository;
import rw.ac.auca.tapwallet.repository.UserRepository;
import rw.ac.auca.tapwallet.repository.WalletRepository;

import java.math.BigDecimal;
import java.util.List;

/**
 * Business rules:
 *  - only an ACTIVE user with the CUSTOMER role can own a wallet, and only one wallet each
 *  - balance changes only through top-up, withdraw and payments (never set directly)
 *  - amounts must be positive; a withdrawal cannot exceed the balance (no overdraft)
 *  - a FROZEN wallet cannot be topped up, withdrawn from, or used to pay
 *  - currency can only change while the balance is zero
 *  - a wallet with a balance or with payment history cannot be deleted
 */
@Service
@Transactional
public class WalletService {

    private final WalletRepository wallets;
    private final UserRepository users;
    private final PaymentRepository payments;

    public WalletService(WalletRepository wallets, UserRepository users, PaymentRepository payments) {
        this.wallets = wallets;
        this.users = users;
        this.payments = payments;
    }

    public Wallet open(WalletRequest request) {
        User owner = users.findById(request.ownerId())
                .orElseThrow(() -> new ResourceNotFoundException("User", request.ownerId()));
        if (owner.getRole() != Role.CUSTOMER) {
            throw new BusinessRuleException("Only users with the CUSTOMER role can own a wallet.");
        }
        if (!owner.isActive()) {
            throw new BusinessRuleException("A frozen user cannot open a wallet.");
        }
        if (wallets.existsByOwnerId(owner.getId())) {
            throw new DuplicateResourceException("User " + owner.getId() + " already has a wallet.");
        }
        return wallets.save(new Wallet(owner, request.openingBalance(), request.currency()));
    }

    @Transactional(readOnly = true)
    public List<Wallet> findAll() {
        return wallets.findAll();
    }

    @Transactional(readOnly = true)
    public Wallet findById(Long id) {
        return wallets.findById(id).orElseThrow(() -> new ResourceNotFoundException("Wallet", id));
    }

    @Transactional(readOnly = true)
    public Wallet findByOwner(Long userId) {
        return wallets.findByOwnerId(userId)
                .orElseThrow(() -> new BusinessRuleException("User " + userId + " does not have a wallet."));
    }

    public Wallet update(Long id, WalletUpdateRequest request) {
        Wallet wallet = findById(id);
        if (!wallet.getCurrency().equals(request.currency()) && wallet.getBalance().signum() != 0) {
            throw new BusinessRuleException("Currency can only be changed while the balance is zero.");
        }
        wallet.setCurrency(request.currency());
        wallet.setStatus(request.status());
        return wallets.save(wallet);
    }

    public Wallet topUp(Long id, BigDecimal amount) {
        Wallet wallet = findById(id);
        requireOperable(wallet, amount);
        wallet.credit(amount);
        return wallets.save(wallet);
    }

    public Wallet withdraw(Long id, BigDecimal amount) {
        Wallet wallet = findById(id);
        requireOperable(wallet, amount);
        if (!wallet.hasEnoughFor(amount)) {
            throw new BusinessRuleException("Insufficient funds: balance is " + wallet.getBalance()
                    + " " + wallet.getCurrency() + ", requested " + amount + ".");
        }
        wallet.debit(amount);
        return wallets.save(wallet);
    }

    public void delete(Long id) {
        Wallet wallet = findById(id);
        if (wallet.getBalance().signum() != 0) {
            throw new BusinessRuleException("Withdraw the remaining balance before closing the wallet.");
        }
        if (payments.existsByWalletId(id)) {
            throw new BusinessRuleException("A wallet with payment history cannot be deleted.");
        }
        wallets.delete(wallet);
    }

    /** Shared guard used by top-up, withdraw and the payment service. */
    void requireOperable(Wallet wallet, BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new BusinessRuleException("Amount must be greater than zero.");
        }
        if (!wallet.isActive()) {
            throw new BusinessRuleException("Wallet " + wallet.getId() + " is frozen.");
        }
        if (!wallet.getOwner().isActive()) {
            throw new BusinessRuleException("The wallet owner's account is frozen.");
        }
    }
}
