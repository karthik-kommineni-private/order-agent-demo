package com.example.orderagent.service.governance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.example.orderagent.config.AgentProperties;
import com.example.orderagent.service.AuditService;
import org.junit.jupiter.api.Test;

/**
 * Verifies {@link AuditInterceptor} writes a redacted audit row for every
 * outcome and drives {@link BreakerRegistry} correctly — a policy block
 * must never count as a tool failure, since the tool never ran.
 */
class AuditInterceptorTest {

    @Test
    void successfulCallResetsTheBreakerAndRecordsASuccessfulAuditRow() {
        AuditService auditService = mock(AuditService.class);
        BreakerRegistry breakerRegistry = new BreakerRegistry(new AgentProperties(6, 20000, 30, 3));
        breakerRegistry.recordFailure("lookupOrder");
        AuditInterceptor interceptor = new AuditInterceptor(auditService, breakerRegistry);

        ToolCallContext ctx = new ToolCallContext("trace-1", "lookupOrder", null);
        ctx.attributes().put("redactedInput", "{\"orderId\":1001}");
        ctx.attributes().put("redactedResult", "{\"status\":\"DELIVERED\"}");

        interceptor.postInvoke(ctx, new ToolOutcome.Success("ok"));

        assertThat(breakerRegistry.isOpen("lookupOrder")).isFalse();
        verify(auditService).record("trace-1", "lookupOrder", "{\"orderId\":1001}", "{\"status\":\"DELIVERED\"}", true);
    }

    @Test
    void failedCallCountsTowardTheBreakerAndRecordsAFailedAuditRow() {
        AuditService auditService = mock(AuditService.class);
        BreakerRegistry breakerRegistry = new BreakerRegistry(new AgentProperties(6, 20000, 30, 3));
        AuditInterceptor interceptor = new AuditInterceptor(auditService, breakerRegistry);

        ToolCallContext ctx = new ToolCallContext("trace-1", "issueRefund", null);

        interceptor.postInvoke(ctx, new ToolOutcome.Failure(new RuntimeException("boom")));
        interceptor.postInvoke(ctx, new ToolOutcome.Failure(new RuntimeException("boom")));
        interceptor.postInvoke(ctx, new ToolOutcome.Failure(new RuntimeException("boom")));

        assertThat(breakerRegistry.isOpen("issueRefund")).isTrue();
        verify(auditService, org.mockito.Mockito.times(3)).record("trace-1", "issueRefund", null, null, false);
    }

    @Test
    void blockedCallDoesNotAffectTheBreakerButStillRecordsAnAuditRow() {
        AuditService auditService = mock(AuditService.class);
        BreakerRegistry breakerRegistry = new BreakerRegistry(new AgentProperties(6, 20000, 30, 3));
        breakerRegistry.recordFailure("issueRefund");
        breakerRegistry.recordFailure("issueRefund");
        AuditInterceptor interceptor = new AuditInterceptor(auditService, breakerRegistry);

        ToolCallContext ctx = new ToolCallContext("trace-1", "issueRefund", null);

        interceptor.postInvoke(ctx, new ToolOutcome.Blocked("refund exceeds order total"));

        // Two prior failures should be untouched — a block isn't a health
        // signal, so it neither resets nor advances the count.
        assertThat(breakerRegistry.isOpen("issueRefund")).isFalse();
        breakerRegistry.recordFailure("issueRefund");
        assertThat(breakerRegistry.isOpen("issueRefund")).isTrue();
        verify(auditService).record("trace-1", "issueRefund", null, null, false);
    }
}
