# 0002 — Drive the agent loop explicitly, not via framework auto-execution

## Context

Older Spring AI versions (and other agent frameworks generally) offer a
single call that internally loops: send a prompt, execute any requested
tools, feed results back, repeat, until the model stops asking for tools.
Spring AI 2.0 removed this (see
`docs/llm-notes/2026-09-09-no-automatic-tool-loop.md`) — but even where
framework auto-execution is available, this project would not use it.

## Decision

`AgentLoop` (`service/agent/AgentLoop.java`) drives the loop as a plain,
visible Java `while` loop: call the model, inspect `stop_reason`, run
requested tools through the governance chain, append results, call again.
Iteration cap and token budget are checked on every pass, in this class,
not delegated to a framework setting.

## Consequences

- Every place the loop can stop — completed, blocked, breaker open,
  iteration cap, budget cap — is a `case` a reader can find and read the
  exact condition for, not a framework default they have to trust.
- Governance interceptors (allowlist, policy, redaction, audit) sit
  directly in this loop's tool-execution step, so there is no way for a
  tool to run without passing through them — an auto-executing framework
  loop would need its own extension point for this, and that extension
  point becomes one more thing to verify is actually wired correctly.
- More code to maintain than "call the client, let it handle tools." That
  cost is accepted deliberately — see CLAUDE.md: "the loop must be visible
  Java code."

## What would change this decision

Nothing about scale would change this — it's a legibility choice, not a
performance one. It would only change if the project's purpose changed
from teaching the guardrail pattern to just shipping a working agent as
fast as possible.
