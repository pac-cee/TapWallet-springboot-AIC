package rw.ac.auca.tapwallet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rw.ac.auca.tapwallet.domain.Wallet;

import java.util.Optional;

public interface WalletRepository extends JpaRepository<Wallet, Long> {

    Optional<Wallet> findByOwnerId(Long userId);

    boolean existsByOwnerId(Long userId);
}
