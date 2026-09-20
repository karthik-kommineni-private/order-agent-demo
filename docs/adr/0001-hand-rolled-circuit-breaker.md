# 0001 — Hand-rolled circuit breaker instead of Resilience4j

## Context

`resilience4j-spring-boot4` is already a project dependency, and its
`CircuitBreaker` is the standard, battle-tested way to stop calling a
failing dependency. `BreakerRegistry` (`service/governance/BreakerRegistry.java`)
does not use it — it's a ~30-line hand-rolled consecutive-failure counter
instead.

## Decision

Implement the breaker as a plain `Map<String, Integer>` of consecutive
failures per tool, checked and incremented directly by
`AuditInterceptor`/`AgentLoop`, rather than wrapping tool calls in a
Resilience4j `CircuitBreaker`.

## Consequences

- The whole mechanism — count, threshold, open, escalate — fits on one
  screen and is a single Javadoc block away from a full explanation. That
  legibility is the point of this project (see CLAUDE.md's thesis: "the
  model reasons; the code enforces the rules").
- It's missing everything a production breaker provides for free: a
  half-open/retry-probe state, time-windowed (not just consecutive)
  failure counting, per-instance vs. shared state, and metrics wiring.
  `docs/production-readiness.md` calls out distributed breaker state
  explicitly as unbuilt for this reason.
- The `resilience4j-spring-boot4` dependency is currently unused — it's
  present as a marker of intent (the Stack table names it as the
  resilience library) rather than as working code. Worth revisiting: drop
  it, or actually swap the implementation in.

## What would change this decision

Any real multi-instance deployment. A single-process demo can get away
with an in-memory map; a fleet of instances behind a load balancer needs
shared breaker state, which is exactly what a maintained library exists to
handle correctly (see `docs/concepts/03-circuit-breaker.md`, "What it does
NOT solve").
