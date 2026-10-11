package rw.ac.auca.tapwallet.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.ac.auca.tapwallet.domain.AccountStatus;
import rw.ac.auca.tapwallet.domain.Merchant;
import rw.ac.auca.tapwallet.domain.Payment;
import rw.ac.auca.tapwallet.domain.PaymentStatus;
import rw.ac.auca.tapwallet.dto.ReportSummary;
import rw.ac.auca.tapwallet.exception.BusinessRuleException;
import rw.ac.auca.tapwallet.repository.MerchantRepository;
import rw.ac.auca.tapwallet.repository.PaymentRepository;
import rw.ac.auca.tapwallet.repository.UserRepository;
import rw.ac.auca.tapwallet.repository.WalletRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class ReportService {

    private static final int MAX_REPORT_DAYS = 366;

    private final PaymentRepository payments;
    private final UserRepository users;
    private final MerchantRepository merchants;
    private final WalletRepository wallets;

    public ReportService(PaymentRepository payments, UserRepository users,
                         MerchantRepository merchants, WalletRepository wallets) {
        this.payments = payments;
        this.users = users;
        this.merchants = merchants;
        this.wallets = wallets;
    }

    public ReportSummary summary(LocalDate requestedFrom, LocalDate requestedTo) {
        ReportPeriod period = period(requestedFrom, requestedTo);
        List<Payment> selected = findPayments(period);
        List<Payment> completed = selected.stream()
                .filter(payment -> payment.getStatus() == PaymentStatus.COMPLETED)
                .toList();
        List<Payment> reversed = selected.stream()
                .filter(payment -> payment.getStatus() == PaymentStatus.REVERSED)
                .toList();

        return new ReportSummary(
                period.from(), period.to(), Instant.now(),
                users.count(), users.findAll().stream().filter(user -> user.getStatus() == AccountStatus.ACTIVE).count(),
                merchants.count(), merchants.findAll().stream().filter(Merchant::isActive).count(),
                selected.size(), completed.size(), reversed.size(),
                sumPayments(completed), sumPayments(reversed),
                wallets.findAll().stream().map(wallet -> wallet.getBalance()).reduce(BigDecimal.ZERO, BigDecimal::add),
                merchants.findAll().stream().map(Merchant::getRevenue).reduce(BigDecimal.ZERO, BigDecimal::add),
                topMerchants(completed), dailyVolume(completed));
    }

    public String paymentCsv(LocalDate requestedFrom, LocalDate requestedTo) {
        ReportPeriod period = period(requestedFrom, requestedTo);
        StringBuilder csv = new StringBuilder("Reference,Date,Payer,Merchant,Merchant code,Amount,Currency,Status,Note\n");
        for (Payment payment : findPayments(period)) {
            csv.append(csv(payment.getReference())).append(',')
                    .append(csv(payment.getCreatedAt().toString())).append(',')
                    .append(csv(payment.getWallet().getOwner().getFullName())).append(',')
                    .append(csv(payment.getMerchant().getBusinessName())).append(',')
                    .append(csv(payment.getMerchant().getCode())).append(',')
                    .append(payment.getAmount().toPlainString()).append(',')
                    .append(csv(payment.getCurrency())).append(',')
                    .append(csv(payment.getStatus().name())).append(',')
                    .append(csv(payment.getNote())).append('\n');
        }
        return csv.toString();
    }

    private List<Payment> findPayments(ReportPeriod period) {
        LocalDateTime from = period.from().atStartOfDay();
        LocalDateTime toExclusive = period.to().plusDays(1).atStartOfDay();
        return payments.findByCreatedAtGreaterThanEqualAndCreatedAtLessThanOrderByCreatedAtDesc(from, toExclusive);
    }

    private ReportPeriod period(LocalDate requestedFrom, LocalDate requestedTo) {
        LocalDate to = requestedTo == null ? LocalDate.now() : requestedTo;
        LocalDate from = requestedFrom == null ? to.minusDays(29) : requestedFrom;
        if (from.isAfter(to)) {
            throw new BusinessRuleException("Report start date cannot be after the end date.");
        }
        if (from.plusDays(MAX_REPORT_DAYS - 1L).isBefore(to)) {
            throw new BusinessRuleException("Report period cannot exceed " + MAX_REPORT_DAYS + " days.");
        }
        return new ReportPeriod(from, to);
    }

    private BigDecimal sumPayments(List<Payment> selected) {
        return selected.stream().map(Payment::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<ReportSummary.MerchantPerformance> topMerchants(List<Payment> completed) {
        Map<Long, MerchantAccumulator> totals = new LinkedHashMap<>();
        for (Payment payment : completed) {
            Merchant merchant = payment.getMerchant();
            totals.computeIfAbsent(merchant.getId(), ignored -> new MerchantAccumulator(merchant))
                    .add(payment.getAmount());
        }
        return totals.values().stream()
                .sorted(Comparator.comparing(MerchantAccumulator::volume).reversed())
                .limit(5)
                .map(item -> new ReportSummary.MerchantPerformance(item.merchant().getId(),
                        item.merchant().getBusinessName(), item.merchant().getCode(),
                        item.payments(), item.volume()))
                .toList();
    }

    private List<ReportSummary.DailyVolume> dailyVolume(List<Payment> completed) {
        Map<LocalDate, DailyAccumulator> totals = new LinkedHashMap<>();
        completed.stream().sorted(Comparator.comparing(Payment::getCreatedAt)).forEach(payment ->
                totals.computeIfAbsent(payment.getCreatedAt().toLocalDate(), ignored -> new DailyAccumulator())
                        .add(payment.getAmount()));
        List<ReportSummary.DailyVolume> result = new ArrayList<>();
        totals.forEach((date, total) -> result.add(new ReportSummary.DailyVolume(date, total.payments, total.volume)));
        return result;
    }

    private String csv(String value) {
        String safe = value == null ? "" : value;
        return '"' + safe.replace("\"", "\"\"") + '"';
    }

    private record ReportPeriod(LocalDate from, LocalDate to) {
    }

    private static final class MerchantAccumulator {
        private final Merchant merchant;
        private long payments;
        private BigDecimal volume = BigDecimal.ZERO;

        private MerchantAccumulator(Merchant merchant) {
            this.merchant = merchant;
        }

        void add(BigDecimal amount) {
            payments++;
            volume = volume.add(amount);
        }

        Merchant merchant() { return merchant; }
        long payments() { return payments; }
        BigDecimal volume() { return volume; }
    }

    private static final class DailyAccumulator {
        private long payments;
        private BigDecimal volume = BigDecimal.ZERO;

        void add(BigDecimal amount) {
            payments++;
            volume = volume.add(amount);
        }
    }
}
