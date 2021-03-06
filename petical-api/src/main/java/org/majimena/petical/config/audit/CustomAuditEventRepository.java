package org.majimena.petical.config.audit;

import org.majimena.petical.domain.PersistentAuditEvent;
import org.majimena.petical.repository.PersistenceAuditEventRepository;
import org.springframework.boot.actuate.audit.AuditEvent;
import org.springframework.boot.actuate.audit.AuditEventRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Wraps an implementation of Spring Boot's AuditEventRepository.
 */
@Repository
public class CustomAuditEventRepository {

    @Inject
    private PersistenceAuditEventRepository persistenceAuditEventRepository;

    @Bean
    public AuditEventRepository auditEventRepository() {
        return new AuditEventRepository() {

            @Inject
            private AuditEventConverter auditEventConverter;

            @Override
            public List<AuditEvent> find(String principal, Date after) {
                Iterable<PersistentAuditEvent> persistentAuditEvents;
                if (principal == null && after == null) {
                    persistentAuditEvents = persistenceAuditEventRepository.findAll();
                } else if (after == null) {
                    persistentAuditEvents = persistenceAuditEventRepository.findByPrincipal(principal);
                } else {
                    LocalDateTime date = LocalDateTime.ofInstant(after.toInstant(), ZoneId.of("UTC"));
                    persistentAuditEvents =
                            persistenceAuditEventRepository.findByPrincipalAndAuditEventDateAfter(principal, date);
                }
                return auditEventConverter.convertToAuditEvent(persistentAuditEvents);
            }

            @Override
            public List<AuditEvent> find(String s, Date date, String s1) {
                return Arrays.asList();
            }

            @Override
            @Transactional(propagation = Propagation.REQUIRES_NEW)
            public void add(AuditEvent event) {
                PersistentAuditEvent persistentAuditEvent = new PersistentAuditEvent();
                persistentAuditEvent.setPrincipal(event.getPrincipal());
                persistentAuditEvent.setAuditEventType(event.getType());
                persistentAuditEvent.setAuditEventDate(LocalDateTime.ofInstant(event.getTimestamp().toInstant(), ZoneId.of("UTC")));
                persistentAuditEvent.setData(auditEventConverter.convertDataToStrings(event.getData()));

                persistenceAuditEventRepository.save(persistentAuditEvent);
            }

            @Override
            public List<AuditEvent> find(Date date) {
                return Arrays.asList();
            }
        };
    }
}
