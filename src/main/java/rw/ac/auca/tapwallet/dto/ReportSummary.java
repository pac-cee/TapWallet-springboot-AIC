package rw.ac.auca.tapwallet.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record ReportSummary(
        LocalDate from,
        LocalDate to,
        Instant generatedAt,
        long totalUsers,
        long activeUsers,
        long totalMerchants,
        long activeMerchants,
        long totalPayments,
        long completedPayments,
        long reversedPayments,
        BigDecimal completedVolume,
        BigDecimal reversedVolume,
        BigDecimal walletBalances,
        BigDecimal merchantRevenue,
        List<MerchantPerformance> topMerchants,
        List<DailyVolume> dailyVolume) {

    public record MerchantPerformance(
            Long merchantId,
            String businessName,
            String code,
            long payments,
            BigDecimal volume) {
    }

    public record DailyVolume(
            LocalDate date,
            long payments,
            BigDecimal volume) {
    }
}
