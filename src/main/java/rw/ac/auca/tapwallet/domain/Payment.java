package rw.ac.auca.tapwallet.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * One payment from a Wallet to a Merchant. A payment is a historical fact:
 * it is recorded once and can only be reversed, never edited.
 */
@Entity
@Table(name = "payment")
public class Payment extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long id;

    @Column(name = "reference", nullable = false, unique = true, length = 20)
    private String reference;

    @ManyToOne(optional = false)
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;

    @ManyToOne(optional = false)
    @JoinColumn(name = "merchant_id", nullable = false)
    private Merchant merchant;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "note", length = 120)
    private String note;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private PaymentStatus status = PaymentStatus.COMPLETED;

    public Payment() {
    }

    public Payment(Wallet wallet, Merchant merchant, BigDecimal amount, String note) {
        this.wallet = wallet;
        this.merchant = merchant;
        this.amount = amount;
        this.currency = wallet.getCurrency();
        this.note = note;
        this.status = PaymentStatus.COMPLETED;
        this.reference = "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    @JsonIgnore
    public boolean isReversed() {
        return status == PaymentStatus.REVERSED;
    }

    public void markReversed() {
        this.status = PaymentStatus.REVERSED;
    }

    public Long getId() {
        return id;
    }

    public String getReference() {
        return reference;
    }

    public Wallet getWallet() {
        return wallet;
    }

    public Merchant getMerchant() {
        return merchant;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public String getNote() {
        return note;
    }

    public PaymentStatus getStatus() {
        return status;
    }
}
