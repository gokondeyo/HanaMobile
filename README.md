# HanaMobile

Local-first Android assistant platform in Kotlin + Jetpack Compose.

## Proposed module/package structure

- `app/`
  - `app/` application bootstrap + DI container
  - `core/model/` canonical data models (`ChatMessage`, `ChatSession`, `PromptPreset`, etc.)
  - `core/session/` session assembly pipeline (`SessionManager`)
  - `core/extensions/` backend/STT/TTS/waveform/tools/multimodal/soul interfaces
  - `domain/repository/` repository abstractions
  - `domain/service/` orchestration services (`MemoryManager`) + mock engines
  - `data/local/` Room database/DAO/entities
  - `data/repository/` repository implementations + mappers
  - `ui/viewmodel/` ViewModel + StateFlow UI states
  - `ui/navigation/` compose navigation graph
  - `ui/screen/` chat, voice, prompt, memory, saved-chats screens

## Architecture summary

- **Dependency inversion**: UI and SessionManager depend on interfaces (`LocalInferenceBackend`, `SpeechToTextEngine`, etc.).
- **Session assembly pipeline**: Request payload is assembled with explicit layers:
  1. system prompt
  2. memory injection block
  3. prior chat history
  4. user input
  5. optional tool results
  6. optional multimodal payload
  7. optional soul-engine context/review hooks
- **Persistence**: Prompt presets, memory entries, chats, and messages are persisted via Room through repository interfaces.
- **State model**: `ViewModel + StateFlow` for chat, prompt management, memory management, saved chats, and voice pipeline.

## Integration points

- Replace `MockLocalInferenceBackend` with llama.cpp / ExecuTorch / MediaPipe provider.
- Replace `MockSpeechToTextEngine` and `MockTextToSpeechEngine` with platform or embedded models.
- Attach web/tools via `ToolRegistry` + `ToolProvider` and inject `ToolResult` in `SessionManager`.
- Attach image support via `ImageInputSource`, `ImagePreprocessor`, and `MultimodalRequestPackager`.
- Attach soul-engine as optional `SoulEngineIntervention` between request assembly and final output.
