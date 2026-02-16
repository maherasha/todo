package org.com.maher.todo.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.com.maher.todo.service.TodoService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class PastDueScheduler {

    private final TodoService todoService;

    @Value("${todo.past-due.batch-size}")
    private int batchSize;

    @Scheduled(fixedRateString = "${todo.past-due.check-interval-ms}")
    public void checkForPastDueItems() {
        Instant cutoff = Instant.now();
        log.info("Past-due check started — cutoff time: {}", cutoff);

        int totalUpdated = 0;
        int updated;
        int batchNumber = 0;
        do {
            batchNumber++;
            updated = todoService.markOverdueAsPastDueBatch(batchSize);
            totalUpdated += updated;
            log.debug("Batch {}: updated {} item(s)", batchNumber, updated);
        } while (updated == batchSize);

        log.info("Past-due check completed — marked {} item(s) as past due in {} batch(es)", totalUpdated, batchNumber);
    }
}
