package com.example.orderagent.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.example.orderagent.entity.AuditLog;
import com.example.orderagent.repository.AuditLogRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

/**
 * Verifies {@link AuditService} persists exactly what it's given — the
 * redaction decision happens upstream in {@code RedactionInterceptor}, so
 * this class only needs to prove the handoff to the repository is faithful.
 */
class AuditServiceTest {

    @Test
    void recordsAnAuditRowWithTheGivenFieldsAndACreationTimestamp() {
        AuditLogRepository repository = mock(AuditLogRepository.class);
        AuditService auditService = new AuditService(repository);

        auditService.record("trace-1", "lookupOrder", "{\"orderId\":1001}", "{\"status\":\"DELIVERED\"}", true);

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(repository).save(captor.capture());
        AuditLog saved = captor.getValue();
        assertThat(saved.getTraceId()).isEqualTo("trace-1");
        assertThat(saved.getToolName()).isEqualTo("lookupOrder");
        assertThat(saved.getArgumentsJson()).isEqualTo("{\"orderId\":1001}");
        assertThat(saved.getResultJson()).isEqualTo("{\"status\":\"DELIVERED\"}");
        assertThat(saved.isSuccess()).isTrue();
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    void recordsAFailedToolCallWithSuccessFalse() {
        AuditLogRepository repository = mock(AuditLogRepository.class);
        AuditService auditService = new AuditService(repository);

        auditService.record("trace-2", "issueRefund", "{\"orderId\":1002}", "no such order", false);

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().isSuccess()).isFalse();
    }
}
