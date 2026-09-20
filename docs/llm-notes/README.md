# LLM notes

A running log of model behavior encountered while building this project —
things that surfaced only from a live call, not from reading the Spring AI
docs. Offline/cassette tests (Phase 6) can't catch these by construction,
since they replay a canned response; every entry here started as something
that broke against the real Anthropic API.

Each entry is short: what happened, why, and the commit or file that fixed
it. This is a log, not a tutorial — see `docs/concepts/` for the taught
version of anything that became a permanent pattern.

| Date | Entry | Model(s) |
|---|---|---|
| 2026-09-09 | [Tool loop is not automatic in Spring AI 2.0](2026-09-09-no-automatic-tool-loop.md) | n/a (framework) |
| 2026-09-09 | [A fresh ChatOptions builder silently drops the configured model](2026-09-09-chat-options-must-be-mutated.md) | claude-sonnet-5 |
| 2026-09-09 | [Without forced tool_choice, the model sometimes answers in prose instead of calling submit_answer](2026-09-09-forced-tool-choice.md) | claude-sonnet-5 |
| 2026-09-09 | [Haiku holds up on the happy path once tool_choice is forced](2026-09-09-haiku-for-demo-cost.md) | claude-haiku-4-5-20251001 |

## Adding an entry

One file per surprise, named `YYYY-MM-DD-short-slug.md`:

```markdown
# <what happened, as a headline>

**Model:** <model id, or "n/a (framework)">
**Where:** <file/class this touched>
**Symptom:** <what you actually saw — an error, wrong output, a silent
default>
**Cause:** <why, in one or two sentences>
**Fix:** <the commit or the one-line change>
```

If a quirk turns out to be a recurring pattern rather than a one-off, graduate
it into a proper `docs/concepts/` file and leave a pointer here instead of
duplicating the explanation.
