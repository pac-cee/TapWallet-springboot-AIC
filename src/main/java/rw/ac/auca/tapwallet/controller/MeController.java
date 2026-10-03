package rw.ac.auca.tapwallet.controller;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ResponseStatus;
import rw.ac.auca.tapwallet.domain.Merchant;
import rw.ac.auca.tapwallet.domain.Payment;
import rw.ac.auca.tapwallet.domain.Role;
import rw.ac.auca.tapwallet.domain.User;
import rw.ac.auca.tapwallet.domain.Wallet;
import rw.ac.auca.tapwallet.dto.AmountRequest;
import rw.ac.auca.tapwallet.dto.CustomerPaymentRequest;
import rw.ac.auca.tapwallet.dto.PaymentRequest;
import rw.ac.auca.tapwallet.service.MerchantService;
import rw.ac.auca.tapwallet.service.PaymentService;
import rw.ac.auca.tapwallet.service.UserService;
import rw.ac.auca.tapwallet.service.WalletService;

import java.util.List;

@RestController
@RequestMapping("/api/me")
public class MeController {

    private final UserService userService;
    private final WalletService walletService;
    private final MerchantService merchantService;
    private final PaymentService paymentService;

    public MeController(UserService userService, WalletService walletService,
                        MerchantService merchantService, PaymentService paymentService) {
        this.userService = userService;
        this.walletService = walletService;
        this.merchantService = merchantService;
        this.paymentService = paymentService;
    }

    @GetMapping
    public User me(@AuthenticationPrincipal Jwt jwt) {
        return userService.findById(userId(jwt));
    }

    @GetMapping("/wallet")
    @PreAuthorize("hasRole('CUSTOMER')")
    public Wallet wallet(@AuthenticationPrincipal Jwt jwt) {
        return walletService.findByOwner(userId(jwt));
    }

    @PostMapping("/wallet/top-up")
    @PreAuthorize("hasRole('CUSTOMER')")
    public Wallet topUp(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody AmountRequest request) {
        Wallet wallet = walletService.findByOwner(userId(jwt));
        return walletService.topUp(wallet.getId(), request.amount());
    }

    @GetMapping("/merchant")
    @PreAuthorize("hasRole('MERCHANT')")
    public Merchant merchant(@AuthenticationPrincipal Jwt jwt) {
        return merchantService.findByOperator(userId(jwt));
    }

    @GetMapping("/payments")
    public List<Payment> payments(@AuthenticationPrincipal Jwt jwt) {
        User user = userService.findById(userId(jwt));
        if (user.getRole() == Role.ADMIN) {
            return paymentService.findAll();
        }
        if (user.getRole() == Role.MERCHANT) {
            return paymentService.findByMerchant(merchantService.findByOperator(user.getId()).getId());
        }
        return paymentService.findByWallet(walletService.findByOwner(user.getId()).getId());
    }

    @PostMapping("/payments")
    @PreAuthorize("hasRole('CUSTOMER')")
    @ResponseStatus(HttpStatus.CREATED)
    public Payment pay(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody CustomerPaymentRequest request) {
        Wallet wallet = walletService.findByOwner(userId(jwt));
        return paymentService.pay(new PaymentRequest(wallet.getId(), request.merchantId(),
                request.amount(), request.note()));
    }

    private Long userId(Jwt jwt) {
        Number claim = jwt.getClaim("userId");
        return claim.longValue();
    }
}
