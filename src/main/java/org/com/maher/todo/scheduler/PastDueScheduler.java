package org.com.maher.todo.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.com.maher.todo.service.TodoService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PastDueScheduler {

    private final TodoService todoService;

    @Value("${todo.past-due.batch-size}")
    private int batchSize;

    @Scheduled(fixedRateString = "${todo.past-due.check-interval-ms}")
    public void checkForPastDueItems() {
        int totalUpdated = 0;
        int updated;
        do {
            updated = todoService.markOverdueAsPastDueBatch(batchSize);
            totalUpdated += updated;
        } while (updated > 0);
        if (totalUpdated > 0) {
            log.info("Marked {} item(s) as past due", totalUpdated);
        }
    }
}
