package rw.ac.auca.tapwallet.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import rw.ac.auca.tapwallet.domain.Payment;
import rw.ac.auca.tapwallet.dto.PaymentRequest;
import rw.ac.auca.tapwallet.service.PaymentService;

import java.util.List;

/**
 * Payments are immutable records, so there is no PUT or DELETE:
 * a payment is created, read, and (at most once) reversed.
 */
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Payment pay(@Valid @RequestBody PaymentRequest request) {
        return paymentService.pay(request);
    }

    @GetMapping
    public List<Payment> findAll() {
        return paymentService.findAll();
    }

    @GetMapping("/{id}")
    public Payment findById(@PathVariable Long id) {
        return paymentService.findById(id);
    }

    @GetMapping("/by-wallet/{walletId}")
    public List<Payment> findByWallet(@PathVariable Long walletId) {
        return paymentService.findByWallet(walletId);
    }

    @GetMapping("/by-merchant/{merchantId}")
    public List<Payment> findByMerchant(@PathVariable Long merchantId) {
        return paymentService.findByMerchant(merchantId);
    }

    @PostMapping("/{id}/reverse")
    public Payment reverse(@PathVariable Long id) {
        return paymentService.reverse(id);
    }
}
