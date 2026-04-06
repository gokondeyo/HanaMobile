# LiteRT-LM backend integration (Android)

This repository uses a real on-device local inference backend based on Google LiteRT-LM runtime APIs (`com.google.mediapipe:tasks-genai`) and supports model selection in-app.

## Where backend code lives

- Backend runtime implementation: `app/src/main/java/com/hanamobile/domain/service/inference/LiteRtLmLocalInferenceBackend.kt`
- Prompt adapter/formatter: `app/src/main/java/com/hanamobile/domain/service/inference/LiteRtLmPromptFormatter.kt`
- Model loading/path validation: `app/src/main/java/com/hanamobile/domain/service/inference/LiteRtLmModelLoader.kt`
- Local model directory scanner: `app/src/main/java/com/hanamobile/domain/service/inference/LocalModelCatalog.kt`
- Backend config + errors: `app/src/main/java/com/hanamobile/core/model/BackendConfig.kt`
- App bootstrap wiring: `app/src/main/java/com/hanamobile/app/HanaApplication.kt`

## Model directory strategy (`media/models`)

The app scans model files from app media storage:

- `${externalMediaDir}/models`
- Typical path: `/storage/emulated/0/Android/media/com.hanamobile/models`

From Prompt Settings screen, you can refresh model list and choose active model file. The selected file name is persisted in app settings.

## Swapping models later

1. Copy new model files into `Android/media/com.hanamobile/models`.
2. Open Prompt Settings → refresh models → choose active model.
3. If needed, tune generation params in `app/build.gradle.kts`:
   - `LITERT_MAX_TOKENS`
   - `LITERT_TOP_K`
   - `LITERT_TEMPERATURE`
   - `LITERT_RANDOM_SEED`

## Failure troubleshooting

- **No models visible**: verify model files exist under `media/models` and tap refresh.
- **Model file missing**: selected model was removed; choose another model in Prompt Settings.
- **Model initialization fails**: check model format compatibility (`.task` / LiteRT-compatible package) and device support.
- **Generation fails**: validate prompt/model pair and generation settings.
