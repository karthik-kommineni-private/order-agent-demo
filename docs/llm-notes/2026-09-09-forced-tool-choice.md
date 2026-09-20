# Without forced tool_choice, the model sometimes answers in prose instead of calling submit_answer

**Model:** claude-sonnet-5
**Where:** `service/agent/AgentLoop.java`, `service/tool/SubmitAnswerTool.java`

**Symptom:** Most live runs ended correctly with a `submit_answer` tool
call, which is what `AgentLoop` requires to produce a final
`AgentResponse`. Occasionally the model would instead reply directly in
plain text — no tool call at all — leaving the loop with nothing to
extract a schema-forced answer from.

**Cause:** By default, a model choosing between "call a tool" and "just
answer in text" is free to pick either, per turn. `submit_answer` exists
specifically so the final answer always arrives in a validated shape (see
`docs/concepts/04-schema-forced-output.md`), but that guarantee only holds
if the model is required to call *some* tool on every turn rather than
merely permitted to.

**Fix:** Set `toolChoice(ToolChoice.ofAny(...))` on every call, forcing the
model to invoke a tool — `lookupOrder`, `issueRefund`, or `submit_answer` —
instead of ever replying in free text. This closed the gap entirely in
subsequent live testing. See commit `16e9525`.

**Note:** this is a mechanical guarantee about *shape*, not about
*correctness* — forcing tool use doesn't stop the model from calling
`submit_answer` with a wrong or unhelpful answer. That's a different
problem, and not one this fix touches.
