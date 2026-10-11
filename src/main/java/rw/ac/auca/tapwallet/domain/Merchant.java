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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.math.BigDecimal;

/**
 * A shop that accepts wallet payments. Operated by exactly one user with the MERCHANT role.
 */
@Entity
@Table(name = "merchant")
public class Merchant extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "merchant_id")
    private Long id;

    @Version
    @Column(name = "version", nullable = false, columnDefinition = "bigint default 0")
    private long version;

    @Column(name = "business_name", nullable = false, length = 80)
    private String businessName;

    @Column(name = "merchant_code", nullable = false, unique = true, length = 7)
    private String code;

    @OneToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User operator;

    @Column(name = "revenue", nullable = false, precision = 19, scale = 2)
    private BigDecimal revenue = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private AccountStatus status = AccountStatus.ACTIVE;

    public Merchant() {
    }

    public Merchant(String businessName, String code, User operator) {
        this.businessName = businessName;
        this.code = code;
        this.operator = operator;
        this.revenue = BigDecimal.ZERO;
        this.status = AccountStatus.ACTIVE;
    }

    @JsonIgnore
    public boolean isActive() {
        return status.isActive();
    }

    public void receive(BigDecimal amount) {
        this.revenue = this.revenue.add(amount);
    }

    public void refund(BigDecimal amount) {
        this.revenue = this.revenue.subtract(amount);
    }

    public Long getId() {
        return id;
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public User getOperator() {
        return operator;
    }

    public void setOperator(User operator) {
        this.operator = operator;
    }

    public BigDecimal getRevenue() {
        return revenue;
    }

    public void setRevenue(BigDecimal revenue) {
        this.revenue = revenue;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public void setStatus(AccountStatus status) {
        this.status = status;
    }
}
