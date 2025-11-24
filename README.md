# VISUS Native (Android)

Minimal szkielet pod przepisanie webowego VISUS na Androida (Kotlin, Compose, OpenGL ES 3.0).

## Wymagania środowiska
- JDK 17+
- Android Studio (Koala/Ladybug) z SDK Platform-Tools
- NDK + CMake (jeśli będzie Oboe / C++)
- Min SDK 26, target 34

## Struktura
- `app/build.gradle.kts` — Compose, CameraX, AudioRecord (JTransforms), OpenGL ES.
- `MainActivity` + `VisusApp` — start Compose.
- `VisusScreen` — podgląd GLSurfaceView + panel sterowania (aspect, record toggle) + start CameraX/audio analyzer.
- `engine/VisusRenderer` — szkielet renderer GL + SurfaceTexture/OES dla CameraX.
- `engine/CameraController` — CameraX -> SurfaceTexture (OES).
- `engine/ShaderRepository` — placeholder na port SHADER_LIST z weba.
- `audio/AudioAnalyzer` — AudioRecord + FFT (JTransforms) z podziałem na bass/mid/high.
- `recording/VisusRecorder` — szkic MediaCodec/Muxer (Surface input) z podpięciem drugiej powierzchni do renderera.
- `engine/EglRecorderSurface` — EGL14 dla drugiego celu renderu (MediaCodec Surface).
- Ikona: adaptive icon z VISUS `icon.png`.

## Następne kroki
1) Uruchomić gradlew (wrapper dodany) i zbudować w Android Studio/CLI.
2) Uzupełnić renderer o programy shaderowe i rysowanie OES -> main shader -> additive stack (obecnie prosty efekt RGB/glitch).
3) Przenieść shadery z web (`constants.ts`) do assets/res/raw, dopasować nagłówki GLSL ES.
4) Zmapować wyniki FFT na uniformy shaderów i do UI.
5) Dokończyć MediaCodec: render sceny na drugi EGLSurface (API przygotowane) i finalizacja pliku MP4.

## Build (CLI)
```
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```
