# Tool loop is not automatic in Spring AI 2.0

**Model:** n/a (framework)
**Where:** `service/agent/AgentLoop.java`

**Symptom:** Following older Spring AI tutorials, the expectation was that
calling `ChatClient` with tools registered would run the whole
lookup-then-answer sequence in one call. It didn't — a single call returns
after the model's first tool-use request and stops there.

**Cause:** Spring AI 2.0 removed the built-in tool-execution loop that
earlier versions ran inside `ChatModel`/`ChatClient` automatically.
`internalToolExecutionEnabled` no longer exists as a flag to turn it back
on — the loop has to be driven by the caller, explicitly, via
`ToolCallingManager`.

**Fix:** `AgentLoop` drives the loop itself: call the model, check
`stop_reason`, if it's `tool_use` run the tool(s) through
`ToolCallingManager` and call the model again with the result appended,
repeat until the model stops asking for tools or a cap is hit. See
`docs/concepts/01-agentic-loop.md` for the full explanation — this ended up
being the central design decision of the project, not an incidental fix.
