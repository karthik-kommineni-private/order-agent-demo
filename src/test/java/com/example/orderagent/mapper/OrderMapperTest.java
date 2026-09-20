package com.example.orderagent.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.orderagent.dto.response.OrderDto;
import com.example.orderagent.entity.Order;
import com.example.orderagent.enums.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

/**
 * Verifies {@link OrderMapper} carries every field from {@link Order} to
 * {@link OrderDto} without loss — the entity must never leave the service
 * layer, so this mapping is the only path customer-facing code has to an
 * order's data.
 */
class OrderMapperTest {

    private final OrderMapper mapper = new OrderMapper();

    @Test
    void mapsEveryFieldFromTheEntityToTheDto() {
        Order order = new Order(
                1001L, "CUST-1001", "alice@example.com", new BigDecimal("89.99"),
                OrderStatus.DELIVERED, LocalDate.of(2026, 8, 14), new BigDecimal("10.00"), "on time");

        OrderDto dto = mapper.toDto(order);

        assertThat(dto.id()).isEqualTo(1001L);
        assertThat(dto.customerId()).isEqualTo("CUST-1001");
        assertThat(dto.customerEmail()).isEqualTo("alice@example.com");
        assertThat(dto.total()).isEqualByComparingTo(new BigDecimal("89.99"));
        assertThat(dto.status()).isEqualTo(OrderStatus.DELIVERED);
        assertThat(dto.shipDate()).isEqualTo(LocalDate.of(2026, 8, 14));
        assertThat(dto.refundedAmount()).isEqualByComparingTo(new BigDecimal("10.00"));
        assertThat(dto.notes()).isEqualTo("on time");
    }

    @Test
    void mapsANullShipDateForAnOrderThatHasNotShippedYet() {
        Order order = new Order(
                1005L, "CUST-1005", "bob@example.com", new BigDecimal("45.00"),
                OrderStatus.PENDING, null, BigDecimal.ZERO, "awaiting fulfillment");

        OrderDto dto = mapper.toDto(order);

        assertThat(dto.shipDate()).isNull();
    }
}
