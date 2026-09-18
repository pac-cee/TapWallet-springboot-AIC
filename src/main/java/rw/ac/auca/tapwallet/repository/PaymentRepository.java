package rw.ac.auca.tapwallet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rw.ac.auca.tapwallet.domain.Payment;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByWalletIdOrderByCreatedAtDesc(Long walletId);

    List<Payment> findByMerchantIdOrderByCreatedAtDesc(Long merchantId);

    boolean existsByWalletId(Long walletId);

    boolean existsByMerchantId(Long merchantId);
}
