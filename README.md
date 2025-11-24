# VISUS Native (Android)

Minimal szkielet pod przepisanie webowego VISUS na Androida (Kotlin, Compose, OpenGL ES 3.0).

## Wymagania œrodowiska
- JDK 17+
- Android Studio (Koala/Ladybug) z SDK Platform-Tools
- NDK + CMake (jeœli bêdzie Oboe / C++)
- Min SDK 26, target 34

## Struktura
- `app/build.gradle.kts` — Compose, CameraX, AudioRecord (JTransforms), OpenGL ES.
- `MainActivity` + `VisusApp` — start Compose.
- `VisusScreen` — podgl¹d GLSurfaceView + prosty panel sterowania.
- `engine/VisusRenderer` — szkielet renderer GL, miejsce na pipeline shaderów.
- `engine/ShaderRepository` — placeholder na port SHADER_LIST z weba.
- `audio/AudioAnalyzer` — szkielet pod AudioRecord + FFT.
- `recording/VisusRecorder` — placeholder pod MediaCodec/Muxer (Surface input).

## Nastêpne kroki
1) Dodaæ wrapper Gradle (`gradle wrapper`) lub otworzyæ w Android Studio i zsynchronizowaæ.
2) Podpi¹æ CameraX -> SurfaceTexture -> OES sampler w rendererze.
3) Przenieœæ shadery z web (`constants.ts`) do assets/res/raw, dopasowaæ nag³ówki GLSL ES.
4) Wpi¹æ AudioRecord + FFT (JTransforms) i mapowaæ bass/mid/high na uniformy.
5) Dodaæ MediaCodec Surface do podwójnego renderu (ekran + nagrywanie).
