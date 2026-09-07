# Forge IDE

Forge is an on-device Android IDE: a single, professionally designed app that
provides the complete Android development loop on a phone or tablet — create or
open a Gradle project, edit Kotlin/Java/XML/Compose, compile debug or release
builds, run unit tests, inspect logs, sign, install and ship.

The product requirements live in [`PRD.md`](./PRD.md).

## Status

**Milestone 0 builds green; Milestone 1 foundation in progress.**

Implemented so far:

- Gradle/Kotlin/Compose Android app (`com.anoy.ide`, min SDK 29).
- Forge design system (quiet warm surfaces, terracotta accent, serif/sans/mono).
- Splash, onboarding (3 pages), welcome/email auth, toolchain setup flow.
- Toolchain marks only compile essentials as required; adb/Git/NDK/extra API
  levels/offline sources are optional.
- **Project wizard + generator**: Empty Compose, Empty Views, Basic Views,
  Library, No Activity; Kotlin/Java; Kotlin DSL/Groovy. Generates a standard
  Android Studio project with its own Gradle wrapper.
- **Project manager + tree**: app-private storage, recent projects, source tree
  (generated/build dirs hidden).
- **Editor shell**: open/edit/save text files from the tree.
- **Build tab**: variant selector (debug/release) and assemble action shell.
- **Gradle wrapper resolver**: reads each project's `gradle-wrapper.properties`
  so Forge builds with the project's own Gradle version (Android Studio parity).
- Supabase schema, RLS policies, edge functions, client/email auth scaffolding.
- Unit tests for wrapper resolution and project generation.

CI (`Android CI`) runs `assembleDebug`, uploads `forge-debug-apk`, and runs unit
tests. It passes.

**Still to build (tracked in `docs/ROADMAP.md`):** real on-device toolchain
installer (parallel/resumable downloads + SHA-256), the Gradle process runtime +
real build engine, syntax-highlighted editor with tabs/LSP, terminal, signing
UI, unit-test explorer, git UI, settings/sync, Supabase Google sign-in, Hilt/Room
wiring, and on-device install via `PackageInstaller`/adb.

## Build

Requires a local Android SDK, JDK 17 and Gradle 8.9+ (CI uses `gradle wrapper`
then `./gradlew :app:assembleDebug`). No Android SDK/JDK exists in the Arena
sandbox, so the source is compiled via GitHub Actions.

```bash
gradle wrapper --gradle-version 8.9
./gradlew :app:assembleDebug
```

## Requirements

- Android 10+ (API 29+)
- arm64-v8a primary target

## Contributing

See [`docs/ARCHITECTURE.md`](./docs/ARCHITECTURE.md) for layout/conventions,
[`docs/ROADMAP.md`](./docs/ROADMAP.md) for the Milestone plan,
[`docs/COMPATIBILITY.md`](./docs/COMPATIBILITY.md) for Gradle parity, and
[`docs/SUPABASE.md`](./docs/SUPABASE.md) for backend setup.
