# Forge IDE

Forge is an on-device Android IDE: a single, professionally designed app that
provides the complete Android development loop on a phone or tablet — create or
open a Gradle project, edit Kotlin/Java/XML/Compose, compile debug or release
builds, run unit tests, inspect logs, sign, install and ship.

The product requirements live in [`PRD.md`](./PRD.md). This repository contains
the app source. The current branch is built from **Milestone 0: Foundations**
(design system, splash, onboarding, auth flow, toolchain setup shell, workspace
shell).

## Status

Milestone 0 — in progress.

Implemented so far:

- Gradle/Kotlin/Compose project for a fresh Android app.
- Forge design system: quiet warm surfaces, terracotta accent, serif display
  type, sans UI, monospace code.
- Splash screen using the Android 12 SplashScreen API.
- Onboarding (3 pages), Welcome/Auth email screen, Toolchain setup screen
  (simulated install flow), and a workspace shell with Files / Editor / Build /
  Terminal bottom navigation.
- M0 roadmap and architecture docs.

Not yet implemented: real Supabase auth, Hilt/Room wiring, actual toolchain
downloader, editor, build engine, terminal, and tests.

## Build

This is an Android project. It requires a local Android SDK, JDK 17 and Gradle
8.9+ to build. No Android SDK or JDK is available in the current Arena sandbox,
so the source is committed as a clean scaffold. A Gradle wrapper is not committed
yet; Android Studio will generate one on first open, or you can run:

```bash
gradle :app:assembleDebug
```

## Requirements

- Android 10+ (API 29+)
- arm64-v8a primary target

## Contributing

See [`docs/ARCHITECTURE.md`](./docs/ARCHITECTURE.md) for the module/package
layout and conventions, [`docs/ROADMAP.md`](./docs/ROADMAP.md) for the
Milestone plan derived from the PRD, and [`docs/SUPABASE.md`](./docs/SUPABASE.md)
for the backend setup and the three secrets/values that may need to be provided.
