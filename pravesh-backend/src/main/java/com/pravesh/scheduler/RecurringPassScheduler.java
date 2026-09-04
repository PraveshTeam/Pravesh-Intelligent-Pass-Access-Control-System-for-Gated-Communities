package com.pravesh.scheduler;

import com.pravesh.entity.VisitorPass;
import com.pravesh.entity.enums.PassStatus;
import com.pravesh.entity.enums.PassType;
import com.pravesh.repository.VisitorPassRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RecurringPassScheduler {

    private static final Logger log = LoggerFactory.getLogger(RecurringPassScheduler.class);

    private final VisitorPassRepository passRepository;

    @Scheduled(cron = "0 0 0 * * *") // midnight every day
    @Transactional
    public void regenerateRecurringPasses() {
        List<VisitorPass> recurring = passRepository
                .findByPassTypeAndStatusIn(PassType.RECURRING_DAILY,
                        List.of(PassStatus.ACTIVE, PassStatus.EXPIRED));

        for (VisitorPass pass : recurring) {
            String oldUuid = pass.getUuid();
            pass.setUuid(UUID.randomUUID().toString());
            pass.setStatus(PassStatus.ACTIVE);
            pass.setValidFrom(LocalDateTime.now());
            pass.setValidUntil(LocalDateTime.now().plusDays(1));
            passRepository.save(pass);

            log.info("Regenerated RECURRING_DAILY pass {} — old UUID {} -> new UUID {}",
                    pass.getId(), oldUuid, pass.getUuid());

            // TODO Day 10/11: call NotificationFeignClient to send the new QR to the resident
        }
    }
}