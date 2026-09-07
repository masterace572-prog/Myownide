# Project compatibility & Gradle

Forge is meant to build the same Android projects you build in Android Studio.
That means it must not hard-code a single Gradle version or a single project
layout.

## Rule: use the project's Gradle wrapper

- Every real Gradle/Android project contains `gradle/wrapper/gradle-wrapper.properties`.
- That file declares the exact Gradle distribution the project needs.
- Forge's build engine must read that file, download/verify the matching Gradle
  version if it is not already installed, and then invoke the project's
  `gradlew` (or the resolved `gradle` binary of that version).

In practice this means:

1. Open a project → read `gradle/wrapper/gradle-wrapper.properties`.
2. Parse `distributionUrl` (e.g. `gradle-8.9-bin.zip`).
3. If that version isn't in the toolchain cache, download it (resumable + SHA-256)
   into `files/toolchain/gradle/<version>/`.
4. Run the build with that version, never with a version Forge "chooses".

Forge ships only a small **Gradle launcher** (the wrapper bootstrap) during
toolchain setup. This keeps the base toolchain small and lets each project bring
its own Gradle version, exactly like a desktop machine using Android Studio.

## Worst-case compatibility

A project that successfully builds with `./gradlew assembleDebug` on a normal
machine, with the same architecture and memory constraints, should build in
Forge. If a project depends on a Gradle plugin or version that only runs on a
later JDK, Forge can request/install that JDK version in a future milestone.

## What Forge expects from a project

- A standard Gradle root with `settings.gradle` / `settings.gradle.kts`.
- A `gradlew` or a Gradle wrapper.
- Standard Android modules under `app/` or any other Gradle module.
- Optional flavors/variants are read from the Gradle model; Forge does not
  force a specific flavor.

## Where this is enforced

The component list in the toolchain screen marks **Gradle launcher** as required
but notes that it is *wrapper-aware*. The planned downloader in Milestone 1 must
implement the wrapper resolution described above.
