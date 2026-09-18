package rw.ac.auca.tapwallet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rw.ac.auca.tapwallet.domain.Merchant;

import java.util.Optional;

public interface MerchantRepository extends JpaRepository<Merchant, Long> {

    Optional<Merchant> findByCode(String code);

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);

    boolean existsByOperatorId(Long userId);
}
