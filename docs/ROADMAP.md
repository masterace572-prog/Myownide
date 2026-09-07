# Forge IDE — Roadmap

Work is derived from [`PRD.md`](../PRD.md) and prioritised by P0 / P1 / P2.

## Milestone 0 — Foundations (4 weeks) **current**

- [x] Android project scaffold (`com.anoy.ide`, min SDK 29, target 35)
- [x] Design system: colors, typography, shapes, Material 3 custom theme
- [x] Splash screen (Android 12 SplashScreen API)
- [x] Onboarding (3 pages, page indicators)
- [x] Welcome, email/password auth flow (UI only, inline validation)
- [ ] Supabase auth (Google via Credential Manager, email/password, session)
- [ ] DataStore persistence for session/setup/theme
- [ ] Room cache layer and repository contracts
- [ ] Hilt dependency graph
- [ ] Supabase schema migrations, RLS policies, edge functions
- [ ] Toolchain manifest/signature pipeline stub

## Milestone 1 — Build loop (8 weeks)

- [ ] Toolchain installer: resumable downloads, SHA-256, verified install
- [ ] Process runtime: foreground service, controlled env, pty JNI
- [x] New project wizard (Empty Compose/Views, Basic Views, Library, No Activity;
      Kotlin/Java, Kotlin DSL/Groovy)
- [x] Project generation + local project manager (files/projects)
- [ ] Project tree file operations, filter build/.gradle (tree listing done)
- [x] Editor shell: open/edit/save text files from project tree
- [ ] Compose/sora editor with syntax highlighting, tabs, search
- [ ] Gradle wrapper per project; `assembleDebug`
- [ ] Install via PackageInstaller on-device
- [ ] Terminal emulator + bundled shell/coreutils

## Milestone 2 — Quality (6 weeks)

- [x] Variant selector/build action shell (real Gradle wiring in M1)
- [ ] Product flavors, signing config UI
- [ ] Release APK/AAB builds, keystore management
- [ ] Unit test explorer (Gradle XML reports), `--tests` filters
- [ ] Problems panel, build/lint diagnostics with source links
- [ ] Logcat viewer
- [ ] Kotlin/Java LSP completion, hover, diagnostics

## Milestone 3 — Beta (4 weeks)

- [ ] Git UI: status, stage, commit, branch, diff, push/pull
- [ ] Settings sync (last-write-wins)
- [ ] Tablet three-pane layout
- [ ] Accessibility pass, Play policy review
- [ ] Closed beta of 500 users

## v1.x

- Instrumented tests, JDWP debugger, NDK CMake projects, XML preview
