# Architecture decision records

Short records of the calls that shaped this codebase and would otherwise
only live in Javadoc or a commit message someone has to go digging for.
Not a full decision log — only calls with a real alternative that was
seriously considered and rejected.

Format: context, decision, consequences, and what would change the
decision. One page max.

| ADR | Decision |
|---|---|
| [0001](0001-hand-rolled-circuit-breaker.md) | Hand-rolled consecutive-failure counter instead of Resilience4j's `CircuitBreaker` |
| [0002](0002-explicit-agent-loop.md) | Drive the tool-calling loop explicitly rather than relying on framework auto-execution |
| [0003](0003-cassette-replay-for-tests.md) | Record/replay cassettes instead of mocking `ChatModel` or requiring a live API key in tests |
