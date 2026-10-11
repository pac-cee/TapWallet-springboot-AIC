package rw.ac.auca.tapwallet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rw.ac.auca.tapwallet.domain.Payment;

import java.time.LocalDateTime;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByWalletIdOrderByCreatedAtDesc(Long walletId);

    List<Payment> findByMerchantIdOrderByCreatedAtDesc(Long merchantId);

    List<Payment> findByCreatedAtGreaterThanEqualAndCreatedAtLessThanOrderByCreatedAtDesc(
            LocalDateTime from, LocalDateTime toExclusive);

    boolean existsByWalletId(Long walletId);

    boolean existsByMerchantId(Long merchantId);
}
