package org.com.maher.todo.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.com.maher.todo.service.TodoService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PastDueScheduler {

    private final TodoService todoService;

    @Scheduled(fixedRateString = "${todo.past-due.check-interval-ms}")
    public void checkForPastDueItems() {
        int count = todoService.markOverdueItemsAsPastDue();
        if (count > 0) {
            log.info("Marked {} item(s) as past due", count);
        }
    }
}
