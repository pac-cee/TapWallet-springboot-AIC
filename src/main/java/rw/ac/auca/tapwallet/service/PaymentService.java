package rw.ac.auca.tapwallet.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.ac.auca.tapwallet.domain.Merchant;
import rw.ac.auca.tapwallet.domain.Payment;
import rw.ac.auca.tapwallet.domain.Wallet;
import rw.ac.auca.tapwallet.dto.PaymentRequest;
import rw.ac.auca.tapwallet.exception.BusinessRuleException;
import rw.ac.auca.tapwallet.exception.ResourceNotFoundException;
import rw.ac.auca.tapwallet.event.NotificationEvent;
import rw.ac.auca.tapwallet.event.NotificationPublisher;
import rw.ac.auca.tapwallet.repository.PaymentRepository;

import java.util.List;
import java.util.Map;

/**
 * Paying spans two aggregates (Wallet and Merchant), so the rule lives here.
 * Business rules:
 *  - a customer cannot pay their own shop
 *  - the merchant must be ACTIVE; the wallet and its owner must be ACTIVE
 *  - the wallet must hold enough balance (no overdraft)
 *  - the debit, the credit and the payment record happen in ONE transaction
 *  - a payment can be reversed exactly once; reversal refunds the wallet and
 *    deducts the merchant's revenue
 */
@Service
@Transactional
public class PaymentService {

    private final PaymentRepository payments;
    private final WalletService walletService;
    private final MerchantService merchantService;
    private final ActivityLogService activityLog;
    private final NotificationPublisher notificationPublisher;

    public PaymentService(PaymentRepository payments, WalletService walletService, MerchantService merchantService,
                          ActivityLogService activityLog, NotificationPublisher notificationPublisher) {
        this.payments = payments;
        this.walletService = walletService;
        this.merchantService = merchantService;
        this.activityLog = activityLog;
        this.notificationPublisher = notificationPublisher;
    }

    public Payment pay(PaymentRequest request) {
        Wallet wallet = walletService.findById(request.walletId());
        Merchant merchant = merchantService.findById(request.merchantId());

        if (wallet.getOwner().getId().equals(merchant.getOperator().getId())) {
            throw new BusinessRuleException("You cannot pay your own shop.");
        }
        if (!merchant.isActive()) {
            throw new BusinessRuleException("Merchant " + merchant.getCode() + " is not accepting payments right now.");
        }
        walletService.requireOperable(wallet, request.amount());
        if (!wallet.hasEnoughFor(request.amount())) {
            throw new BusinessRuleException("Insufficient funds: balance is " + wallet.getBalance()
                    + " " + wallet.getCurrency() + ", payment is " + request.amount() + ".");
        }

        wallet.debit(request.amount());
        merchant.receive(request.amount());
        Payment payment = payments.save(new Payment(wallet, merchant, request.amount(), request.note()));
        activityLog.record("PAYMENT_COMPLETED", wallet.getOwner().getId(), "Payment", payment.getId(),
                "Payment " + payment.getReference() + " completed",
                Map.of("amount", payment.getAmount().toPlainString(), "currency", payment.getCurrency(),
                        "merchantCode", merchant.getCode()));
        notificationPublisher.publish(NotificationEvent.of("payment.completed", wallet.getOwner().getEmail(),
                "Your payment of " + payment.getAmount() + " " + payment.getCurrency() + " to "
                        + merchant.getBusinessName() + " was completed.",
                Map.of("reference", payment.getReference())));
        return payment;
    }

    @Transactional(readOnly = true)
    public List<Payment> findAll() {
        return payments.findAll();
    }

    @Transactional(readOnly = true)
    public Payment findById(Long id) {
        return payments.findById(id).orElseThrow(() -> new ResourceNotFoundException("Payment", id));
    }

    @Transactional(readOnly = true)
    public List<Payment> findByWallet(Long walletId) {
        walletService.findById(walletId);
        return payments.findByWalletIdOrderByCreatedAtDesc(walletId);
    }

    @Transactional(readOnly = true)
    public List<Payment> findByMerchant(Long merchantId) {
        merchantService.findById(merchantId);
        return payments.findByMerchantIdOrderByCreatedAtDesc(merchantId);
    }

    public Payment reverse(Long id) {
        Payment payment = findById(id);
        if (payment.isReversed()) {
            throw new BusinessRuleException("Payment " + payment.getReference() + " has already been reversed.");
        }
        payment.markReversed();
        payment.getMerchant().refund(payment.getAmount());
        payment.getWallet().credit(payment.getAmount());
        Payment saved = payments.save(payment);
        activityLog.record("PAYMENT_REVERSED", payment.getWallet().getOwner().getId(), "Payment", payment.getId(),
                "Payment " + payment.getReference() + " reversed",
                Map.of("amount", payment.getAmount().toPlainString(), "currency", payment.getCurrency()));
        notificationPublisher.publish(NotificationEvent.of("payment.reversed",
                payment.getWallet().getOwner().getEmail(),
                "Payment " + payment.getReference() + " was reversed and refunded.",
                Map.of("reference", payment.getReference())));
        return saved;
    }
}
