# Haiku holds up on the happy path once tool_choice is forced

**Model:** claude-haiku-4-5-20251001 (switched from claude-sonnet-5)
**Where:** `src/main/resources/application.yml`

**Symptom:** n/a — this wasn't a bug, it was a cost/latency decision
verified live rather than assumed.

**Cause:** Sonnet is unnecessarily expensive and slow for a demo whose
tool-calling logic is simple (two tools, a handful of fixed orders). The
open question was whether a cheaper model would still reliably resolve the
happy-path scenario (lookup → submit_answer) now that `tool_choice=any` is
forced every turn — a smaller model skipping the tool call would have been
a silent regression back into the failure mode described in
`2026-09-09-forced-tool-choice.md`.

**Fix:** Switched the default model in `application.yml` and re-ran the
happy-path scenario live. It resolved correctly with the same
`lookupOrder → submit_answer` sequence. Worth re-verifying live if either
the system prompt or the tool schemas change meaningfully — this note
records a point-in-time check, not a permanent guarantee about Haiku's
behavior on more complex multi-step scenarios (e.g. the refund-if-shipped-
late case), which was not separately re-verified at model switch time.
