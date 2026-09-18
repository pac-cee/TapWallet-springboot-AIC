package rw.ac.auca.tapwallet.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import rw.ac.auca.tapwallet.domain.Merchant;
import rw.ac.auca.tapwallet.domain.Role;
import rw.ac.auca.tapwallet.domain.User;
import rw.ac.auca.tapwallet.domain.Wallet;
import rw.ac.auca.tapwallet.repository.MerchantRepository;
import rw.ac.auca.tapwallet.repository.UserRepository;
import rw.ac.auca.tapwallet.repository.WalletRepository;

import java.math.BigDecimal;

/**
 * Seeds demo data on every start so the Postman collection works immediately.
 * Users 1-5, wallets 1-2 (Alice, Eric), merchants 1-2 (Cafe, Shop).
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository users;
    private final WalletRepository wallets;
    private final MerchantRepository merchants;

    public DataSeeder(UserRepository users, WalletRepository wallets, MerchantRepository merchants) {
        this.users = users;
        this.wallets = wallets;
        this.merchants = merchants;
    }

    @Override
    public void run(String... args) {
        if (users.count() > 0) {
            return;
        }
        users.save(new User("System Admin", "admin@tapwallet.rw", "+250788000001", Role.ADMIN));
        User alice = users.save(new User("Alice Uwase", "alice@tapwallet.rw", "+250788000002", Role.CUSTOMER));
        User eric = users.save(new User("Eric Mugisha", "eric@tapwallet.rw", "+250788000003", Role.CUSTOMER));
        User cafe = users.save(new User("Kigali Cafe Ltd", "cafe@tapwallet.rw", "+250788000004", Role.MERCHANT));
        User shop = users.save(new User("Nyarugenge Shop", "shop@tapwallet.rw", "+250788000005", Role.MERCHANT));

        wallets.save(new Wallet(alice, new BigDecimal("50000.00"), "RWF"));
        wallets.save(new Wallet(eric, new BigDecimal("12000.00"), "RWF"));

        merchants.save(new Merchant("Kigali Cafe", "CAF-001", cafe));
        merchants.save(new Merchant("Nyarugenge Shop", "SHP-002", shop));
    }
}
