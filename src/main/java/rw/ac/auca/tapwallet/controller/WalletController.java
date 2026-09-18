package rw.ac.auca.tapwallet.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import rw.ac.auca.tapwallet.domain.Wallet;
import rw.ac.auca.tapwallet.dto.AmountRequest;
import rw.ac.auca.tapwallet.dto.WalletRequest;
import rw.ac.auca.tapwallet.dto.WalletUpdateRequest;
import rw.ac.auca.tapwallet.service.WalletService;

import java.util.List;

@RestController
@RequestMapping("/api/wallets")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Wallet open(@Valid @RequestBody WalletRequest request) {
        return walletService.open(request);
    }

    @GetMapping
    public List<Wallet> findAll() {
        return walletService.findAll();
    }

    @GetMapping("/{id}")
    public Wallet findById(@PathVariable Long id) {
        return walletService.findById(id);
    }

    @GetMapping("/by-owner/{userId}")
    public Wallet findByOwner(@PathVariable Long userId) {
        return walletService.findByOwner(userId);
    }

    @PutMapping("/{id}")
    public Wallet update(@PathVariable Long id, @Valid @RequestBody WalletUpdateRequest request) {
        return walletService.update(id, request);
    }

    @PostMapping("/{id}/top-up")
    public Wallet topUp(@PathVariable Long id, @Valid @RequestBody AmountRequest request) {
        return walletService.topUp(id, request.amount());
    }

    @PostMapping("/{id}/withdraw")
    public Wallet withdraw(@PathVariable Long id, @Valid @RequestBody AmountRequest request) {
        return walletService.withdraw(id, request.amount());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        walletService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
