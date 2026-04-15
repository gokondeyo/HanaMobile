# LiteRT-LM backend integration (Android)

This repository uses a real on-device local inference backend based on the official LiteRT-LM Kotlin API.

## Dependency

- Gradle artifact: `com.google.ai.edge.litertlm:litertlm-android:0.10.1`
- Declared in: `app/build.gradle.kts`

The backend uses typed LiteRT-LM classes directly (`Engine`, `EngineConfig`, `ConversationConfig`, `SamplerConfig`). No runtime reflection is used.

## Where backend code lives

- Backend runtime implementation: `app/src/main/java/com/hanamobile/domain/service/inference/LiteRtLmLocalInferenceBackend.kt`
- Prompt adapter/formatter: `app/src/main/java/com/hanamobile/domain/service/inference/LiteRtLmPromptFormatter.kt`
- Model loading/path validation: `app/src/main/java/com/hanamobile/domain/service/inference/LiteRtLmModelLoader.kt`
- Local model directory scanner: `app/src/main/java/com/hanamobile/domain/service/inference/LocalModelCatalog.kt`
- Backend config + errors: `app/src/main/java/com/hanamobile/core/model/BackendConfig.kt`
- App bootstrap wiring: `app/src/main/java/com/hanamobile/app/HanaApplication.kt`

## Model directory strategy (`Android/media/.../models`)

The app scans model files from app media storage:

- `${externalMediaDir}/models`
- Typical path: `/storage/emulated/0/Android/media/com.hanamobile/models`

From Prompt Settings screen, users can refresh model list and choose an active model file by filename (no rename required).

Supported file extensions:

- `.litertlm`

## Generation settings

Configured in `app/build.gradle.kts` as BuildConfig fields and mapped in `HanaApplication`:

- `LITERT_MAX_TOKENS`
- `LITERT_TOP_K`
- `LITERT_TOP_P`
- `LITERT_TEMPERATURE`
- `LITERT_RANDOM_SEED`

Unsupported or invalid values are surfaced as backend errors to UI state.

## Swapping models

1. Copy model file(s) into `Android/media/com.hanamobile/models`.
2. Open Prompt Settings.
3. Tap **Refresh Models**.
4. Select a model and tap **Use**.

The selected model filename is stored in app settings and used by `LiteRtLmModelLoader` at generation time.

## Failure troubleshooting

- **No models visible**: ensure files are in `Android/media/com.hanamobile/models` and use a supported extension.
- **Model file missing**: selected file was removed; choose another model in Prompt Settings.
- **Unsupported model file**: file extension is not `.litertlm`.
- **Model initialization fails**: model/device/runtime mismatch, invalid model package, or native init failure.
- **Generation fails**: check prompt length/model compatibility and generation parameter values.
