# 0003 — Cassette record/replay instead of mocked ChatModel or live API in tests

## Context

CLAUDE.md rule 10: tests must run without `ANTHROPIC_API_KEY` set, and
`mvn clean verify` must be green in that state. Two simpler alternatives
were available: mock `ChatModel` entirely (as `AgentLoopTest` already does
for loop-control-flow tests), or gate live-API tests behind a profile
that's off by default.

## Decision

For the one test that proves the *whole* request-to-tool-execution path
(`AgentLoopGoldenTest`), record a real Anthropic response to
`src/test/resources/cassettes/*.json` once, then replay it through
`CassetteChatModel` — everything downstream of the model call (real
`ToolCallingManager`, real `ToolRegistry`, real governance chain) still
runs for real. Only the model call itself is canned.

## Consequences

- This is a meaningfully stronger test than a fully mocked `ChatModel`: a
  mock only proves `AgentLoop`'s own control flow is correct in isolation,
  while a cassette proves the recorded model output still resolves
  correctly through the real tool registry and real interceptors today —
  catches, for example, a tool schema change that would silently break
  deserialization of the recorded arguments.
- It is not proof the *live* model still behaves the same way. A cassette
  recorded in September could go stale if a later model version changes
  its tool-calling behavior; nothing re-verifies that automatically. See
  `docs/concepts/06-determinism.md` for the full boundary of what this
  does and doesn't guarantee.
- Recording a new cassette requires a live call (`CassetteRecordingTool`,
  test-only, gated behind a profile) — a manual, occasional step, not part
  of the normal test run.

## What would change this decision

A CI pipeline that could safely hold a real API key and re-run live
end-to-end tests on a schedule would close the staleness gap this
approach accepts. That's listed in `docs/production-readiness.md`
("evaluation set with a regression gate") as not built.
