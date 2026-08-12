package com.pravesh.scheduler;

import com.pravesh.repository.VisitorPassRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class PassExpiryScheduler {

    private static final Logger log = LoggerFactory.getLogger(PassExpiryScheduler.class);

    private final VisitorPassRepository passRepository;

    @Scheduled(cron = "0 * * * * *") // every minute, on the minute
    @Transactional
    public void expireOldPasses() {
        int updated = passRepository.expirePassesPastDue(LocalDateTime.now());
        if (updated > 0) {
            log.info("Expired {} passes past their validUntil", updated);
        }
    }
}