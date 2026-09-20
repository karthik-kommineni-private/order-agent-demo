# A fresh ChatOptions builder silently drops the configured model

**Model:** claude-sonnet-5
**Where:** `service/agent/AgentLoop.java`

**Symptom:** `application.yml` had `spring.ai.anthropic.chat.options.model`
set, but live calls were going to the provider's default model instead —
no error, no log line, just the wrong model quietly answering every
request. Offline tests didn't catch this because the cassette replay
doesn't call a real model at all.

**Cause:** The original code built tool-calling options from scratch:
`ToolCallingChatOptions.builder().toolCallbacks(...).build()`. A bare
`builder()` starts from framework defaults, not from what's configured in
`application.yml` — the configured model name lives on the `ChatModel`'s
own default options object, and a fresh builder never sees it.

**Fix:** Mutate the model's existing default options instead of
constructing new ones: `chatModel.getDefaultOptions()`, cast to
`AnthropicChatOptions`, call `.mutate()`, then add the tool callbacks and
tool choice on top. This preserves everything set in config (model,
temperature, etc.) while still layering in the per-call tool settings. See
commit `16e9525`.
