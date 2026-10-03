package rw.ac.auca.tapwallet.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.ac.auca.tapwallet.domain.AccountStatus;
import rw.ac.auca.tapwallet.domain.Merchant;
import rw.ac.auca.tapwallet.domain.Role;
import rw.ac.auca.tapwallet.domain.User;
import rw.ac.auca.tapwallet.dto.MerchantRequest;
import rw.ac.auca.tapwallet.exception.BusinessRuleException;
import rw.ac.auca.tapwallet.exception.DuplicateResourceException;
import rw.ac.auca.tapwallet.exception.ResourceNotFoundException;
import rw.ac.auca.tapwallet.repository.MerchantRepository;
import rw.ac.auca.tapwallet.repository.PaymentRepository;
import rw.ac.auca.tapwallet.repository.UserRepository;

import java.util.List;

/**
 * Business rules:
 *  - merchant code is unique and must look like CAF-001
 *  - the operator must be an ACTIVE user with the MERCHANT role, and operates at most one shop
 *  - revenue is only changed by payments and reversals (never set directly)
 *  - a FROZEN merchant does not accept payments
 *  - a merchant with payment history cannot be deleted
 */
@Service
@Transactional
public class MerchantService {

    private final MerchantRepository merchants;
    private final UserRepository users;
    private final PaymentRepository payments;

    public MerchantService(MerchantRepository merchants, UserRepository users, PaymentRepository payments) {
        this.merchants = merchants;
        this.users = users;
        this.payments = payments;
    }

    public Merchant register(MerchantRequest request) {
        String code = request.code().trim().toUpperCase();
        if (merchants.existsByCode(code)) {
            throw new DuplicateResourceException("Merchant code " + code + " is already in use.");
        }
        User operator = requireOperator(request.operatorId());
        if (merchants.existsByOperatorId(operator.getId())) {
            throw new DuplicateResourceException("User " + operator.getId() + " already operates a merchant shop.");
        }
        return merchants.save(new Merchant(request.businessName().trim(), code, operator));
    }

    @Transactional(readOnly = true)
    public List<Merchant> findAll() {
        return merchants.findAll();
    }

    @Transactional(readOnly = true)
    public Merchant findById(Long id) {
        return merchants.findById(id).orElseThrow(() -> new ResourceNotFoundException("Merchant", id));
    }

    @Transactional(readOnly = true)
    public Merchant findByOperator(Long userId) {
        return merchants.findByOperatorId(userId)
                .orElseThrow(() -> new BusinessRuleException("User " + userId + " does not operate a merchant shop."));
    }

    public Merchant update(Long id, MerchantRequest request) {
        Merchant merchant = findById(id);
        String code = request.code().trim().toUpperCase();
        if (merchants.existsByCodeAndIdNot(code, id)) {
            throw new DuplicateResourceException("Merchant code " + code + " is already in use by another shop.");
        }
        if (!merchant.getOperator().getId().equals(request.operatorId())) {
            User newOperator = requireOperator(request.operatorId());
            if (merchants.existsByOperatorId(newOperator.getId())) {
                throw new DuplicateResourceException("User " + newOperator.getId() + " already operates a merchant shop.");
            }
            merchant.setOperator(newOperator);
        }
        merchant.setBusinessName(request.businessName().trim());
        merchant.setCode(code);
        return merchants.save(merchant);
    }

    public Merchant freeze(Long id) {
        Merchant merchant = findById(id);
        merchant.setStatus(AccountStatus.FROZEN);
        return merchants.save(merchant);
    }

    public Merchant activate(Long id) {
        Merchant merchant = findById(id);
        merchant.setStatus(AccountStatus.ACTIVE);
        return merchants.save(merchant);
    }

    public void delete(Long id) {
        Merchant merchant = findById(id);
        if (payments.existsByMerchantId(id)) {
            throw new BusinessRuleException("A merchant with payment history cannot be deleted. Freeze it instead.");
        }
        merchants.delete(merchant);
    }

    private User requireOperator(Long userId) {
        User operator = users.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        if (operator.getRole() != Role.MERCHANT) {
            throw new BusinessRuleException("Only users with the MERCHANT role can operate a shop.");
        }
        if (!operator.isActive()) {
            throw new BusinessRuleException("A frozen user cannot operate a shop.");
        }
        return operator;
    }
}
