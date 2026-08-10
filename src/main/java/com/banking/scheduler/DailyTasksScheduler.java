package com.banking.scheduler;

import com.banking.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Scheduled jobs that run once per day for banking housekeeping.
 *
 * <p>BUG FIX — daily transfer limit never reset:
 * {@link com.banking.entity.Account#transferredToday} was incremented on
 * every transfer but there was no job to reset it back to zero.
 * After a customer used their daily limit on day 1, they could never
 * transfer again — the counter just kept growing forever.
 *
 * <p>This scheduler fires at midnight server time and bulk-resets the column
 * to zero for all accounts in a single UPDATE query (efficient, no N+1 load).
 *
 * <p>Requires {@code @EnableScheduling} on the application class — already
 * present in {@code BankingApplication}.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DailyTasksScheduler {

    private final AccountRepository accountRepository;

    /**
     * Resets the {@code transferred_today} counter on every account at midnight.
     *
     * <p>Cron expression {@code "0 0 0 * * *"} means: second=0, minute=0,
     * hour=0 (midnight), every day, every month, every weekday.
     */
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void resetDailyTransferLimits() {
        log.info("Midnight job: resetting daily transfer counters for all accounts");
        int updated = accountRepository.resetAllDailyTransferLimits();
        log.info("Midnight job: reset transferredToday to 0 for {} accounts", updated);
    }
}
