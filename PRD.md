#### Product Requirements Document: Forge IDE (working name), a mobile Android IDE for building Android apps on-device

**Version** 1.0 draft | **Status** Proposal | **Platform** Android 10+ (API 29+), arm64-v8a primary | **Backend** Supabase (free tier) | **Date** 2026-09-06

---

#### 1. Vision and problem statement

Developers, students and hobbyists increasingly have a capable ARM64 phone or tablet but no laptop at hand. Today, building a real Android app on a phone means stitching together Termux, hand-installed toolchains and a text editor with no build integration. Forge IDE is a single, professionally designed application that provides the complete Android development loop on the device itself: create or open a Gradle project, edit Kotlin/Java/XML/Compose, compile debug or release builds, run unit tests, inspect logs, sign, install and ship. The experience is calm, premium and focused, in the spirit of the Claude Android app: warm neutral surfaces, restrained accent color, serif display type, no visual noise.

**One-line goal:** "Android Studio's core workflow, redesigned for a 6-inch screen, fully working offline after a one-time toolchain setup."

---

#### 2. Goals and non-goals

**Goals (v1.0)**
1. Build a real Gradle-based Android project (Kotlin or Java, Views or Compose) to a signed APK entirely on-device.
2. First-run toolchain setup that downloads, verifies and installs JDK, Android SDK (platform, build-tools, platform-tools), Gradle and optionally NDK with resumable downloads.
3. Code editor with syntax highlighting, basic completion, diagnostics, multi-file tabs, and search.
4. Integrated terminal with the same environment the build system uses.
5. Run JVM unit tests and display results in a structured UI.
6. Choose build variant (debug/release), product flavors, and signing configs.
7. Account system via Supabase: Google sign-in and email/password, used for settings sync, project metadata sync and license/entitlement flags.
8. A design system faithful to the Claude Android app aesthetic: no gradients, no neon, no emojis, no pill-shaped colored text chips, professional typography.

**Non-goals (v1.0)**
- Layout visual designer (drag and drop). Provide XML/Compose preview only as a stretch item.
- On-device emulator. Apps run on the same device or via ADB over Wi-Fi to a second device.
- Full NDK C++ toolchain parity with desktop (offer clang for arm64 only; x86 ABIs excluded).
- Storing full project source in Supabase (free-tier storage is 1 GB total; use Git remotes for source).
- iOS or desktop clients.

---

#### 3. Target users

| Persona | Need | Success looks like |
|---|---|---|
| **Student (primary)** | Learn Android without a laptop | Creates a project from a template, builds and installs a debug APK in under 10 minutes after setup |
| **Working developer on the move** | Fix a bug, run tests, push a commit from a phone | Clones a repo, edits, runs `testDebugUnitTest`, commits and pushes |
| **Hobbyist / indie** | Ship small apps end to end | Produces a signed release APK and AAB with their own keystore |
| **Educator** | Teach in low-resource environments | Offline builds after one setup; consistent toolchain versions across a class |

---

#### 4. Competitive landscape (context, not requirements)

- **AndroidIDE**: proves on-device Gradle builds are viable with a ported OpenJDK and ARM build tools. Weak points: dated UI, rough onboarding, no accounts.
- **Termux + editors**: maximum flexibility, zero integration, steep learning curve.
- **Acode / Spck / Dcoder**: editors or cloud compilers, not full local Android builds.

Forge differentiates on: integrated setup, premium UI, first-class terminal, test runner, variant management, and an account layer.

---

#### 5. Technical feasibility and architecture

**Core constraint:** Android has no system JVM. The app must ship or download a JDK compiled for Android/Bionic (arm64), the same approach used by Termux packages and AndroidIDE.

**Architecture layers**

1. **App shell (Kotlin, Jetpack Compose)**: navigation, design system, editor, terminal UI, build/test UIs, settings, auth.
2. **Toolchain manager**: downloads component manifests, verifies SHA-256, extracts into app-private storage (`/data/data/<pkg>/files/usr` style prefix), manages versions and repair.
3. **Process runtime**: spawns tool processes (`java`, `gradle` wrapper, `aapt2`, `d8`, `apksigner`, `adb`, `clang`) with a controlled environment (`JAVA_HOME`, `ANDROID_HOME`, `PATH`, `TMPDIR`, `HOME`). Runs in a foreground service with a persistent notification so builds survive backgrounding.
4. **Build engine**: Gradle wrapper per project, with a Gradle daemon kept warm while the IDE is in foreground; Tooling API client for structured progress and problem reporting. Fallback: raw process output parser.
5. **Language services**: bundled Kotlin and Java language servers (or a lightweight in-process indexer for v1) exposing completion, hover and diagnostics through LSP over local sockets.
6. **Terminal**: VT100/xterm emulator with pseudo-terminal (forkpty via JNI), same environment as the build engine.
7. **Backend (Supabase)**: Auth, Postgres with Row Level Security, small Storage bucket for user avatars and exported settings. No source code stored.

**Storage layout (device)**
- `files/toolchain/jdk/<version>`, `files/toolchain/sdk/`, `files/toolchain/gradle/<version>`, `files/toolchain/ndk/<version>`
- `files/home/` (terminal HOME, `.gradle` cache, Git config)
- Projects: user-chosen directory via SAF or app-private `files/projects/`. Default to app-private for performance; export via Share/SAF.

**Key platform risks and mitigations**
- **Google Play policy on downloaded executable code**: distribute the JDK/SDK as a "toolchain" downloaded from our own signed manifest; document policy review path. Ship primary build on Play, mirror on GitHub Releases and F-Droid-compatible build to de-risk removal. Legal review required before launch.
- **Memory**: Gradle plus Kotlin compiler can exceed 2 GB. Require 4 GB RAM minimum, recommend 6 GB+. Default `org.gradle.jvmargs=-Xmx1536m`, configurable. Disable Gradle parallel by default on devices under 6 GB.
- **Storage**: Full toolchain 1.5 to 3 GB. Show disk requirements before download; allow SD/external only for project files, never for executables (noexec).
- **Android 10+ W^X restrictions**: All native executables must live under the app's `files/` (or `lib/`) and be extracted by the app; verify `targetSdk` behavior with `exec()` from app data on each Android version in CI.
- **Battery and thermal**: Builds run in a foreground service with an option to pause on thermal throttling notifications.

---

#### 6. Functional requirements

Priority: **P0** required for v1.0, **P1** target v1.x, **P2** later.

**6.1 Splash screen (P0)**
- Uses the Android 12 SplashScreen API for cold start; on older versions, a themed activity.
- Content: the wordmark "Forge" set in the display serif, centered, on the ivory (light) or charcoal (dark) background. Optional single animation: wordmark fades in over 250 ms and settles with a subtle 8 dp upward motion. No logo spin, no gradients, no progress bar.
- Duration: minimum 600 ms, maximum 1500 ms; hides as soon as the session check completes.
- Routes to Onboarding (first launch), Welcome/Auth (no session), Setup (session but toolchain incomplete), or Workspace (ready).

**6.2 Onboarding (P0)**
- Three horizontally paged screens, each with a large serif heading, two-line body, and a single monochrome line illustration or a device screenshot in a rounded frame.
  1. "Build Android apps on Android." (value proposition)
  2. "A real toolchain, installed once." (what the setup does, size estimate)
  3. "Your work stays on your device." (privacy; account is for sync only)
- Controls: text button "Skip" top right, primary button "Continue" bottom, page indicator as three 6 dp dots in neutral tones.
- Shown once; re-openable from Settings > About.

**6.3 Welcome and authentication (P0)**
- Welcome screen: heading "Welcome to Forge", short body, two stacked buttons: "Continue with Google" (outlined, Google mark in monochrome permitted by brand rules) and "Continue with email" (filled, accent). Secondary text link "Continue without an account" (local-only mode with sync features disabled).
- **Google sign-in**: Supabase Auth OAuth provider via Credential Manager (Android) with ID token exchange (`signInWithIdToken`). Web fallback via Custom Tabs.
- **Email/password**: sign up, sign in, forgot password (Supabase magic link/reset email), email verification banner. Password rules: 8+ chars; show inline validation, never modal errors.
- Session persistence with encrypted storage (Android Keystore backed).
- Account linking: if a Google email matches an existing email account, link identities rather than creating a duplicate.
- Sign out and delete account (required by Play policy) via Supabase edge function that cascades deletes.

**6.4 Toolchain setup (P0)**
- Entry after auth on first run, and from Settings > Toolchain later.
- Screen "Set up your toolchain": a list of components with name, version, size, and a status label. Checkbox selection for optional items.
  - **Required**: OpenJDK 17 (arm64 Android build), Android SDK Platform (latest stable API), Build-Tools (aapt2, d8, apksigner, zipalign), Platform-Tools (adb), Gradle (LTS matching AGP), Kotlin compiler.
  - **Optional**: NDK (arm64 clang, CMake, Ninja), additional API levels, Sources for docs, Git.
- Pre-flight checks: free storage, RAM, network type (warn on metered), battery.
- Download engine: parallel downloads (max 3), resumable via HTTP Range, SHA-256 verification against a signed manifest, retry with exponential backoff, foreground service with notification progress. User may background the app.
- Post-install verification: run `java -version`, `aapt2 version`, `gradle --version`, `adb version`; display green checkmarks as text labels ("Verified") not icons alone.
- Repair, update and uninstall per component. Manifest fetched from our CDN with a signature; pinned public key in the app.
- Completion screen: "Toolchain ready" with total size and a "Create your first project" primary action.

**6.5 Project management (P0)**
- Home screen lists recent projects (name, path, last opened, AGP version) with swipe-to-remove-from-list.
- **New project wizard**: templates: Empty Compose Activity, Empty Views Activity, Basic Views with Navigation, Library module, No Activity. Fields: name, package, save location, language (Kotlin/Java), min SDK (slider with device coverage text), build config language (Kotlin DSL/Groovy).
- **Open project**: from app storage, SAF folder picker, or Git clone (HTTPS with token, SSH key generation in-app).
- Project tree side panel: file operations (new file/folder, rename, delete, move), filters (hide build/.gradle), long-press context menu.
- Gradle sync on open with structured progress and a Problems panel.

**6.6 Code editor (P0)**
- Engine: custom Compose-based editor or a proven Android editor component (e.g., sora-editor style) with tree-sitter grammars.
- Languages: Kotlin, Java, XML, Groovy, Kotlin DSL, JSON, Markdown, Properties, C/C++ (NDK), Shell.
- Features: syntax highlighting, line numbers, bracket matching, auto-indent, code folding, multi-cursor (P1), find/replace with regex, go to line, symbol outline, tabs with unsaved indicator (dot), split view on tablets (P1).
- Language intelligence: Kotlin and Java completion, signature help, hover docs, error/warning squiggles from LSP; XML attribute completion from SDK; resource reference resolution (`R.string.x`) (P1).
- Quick fixes: import class, create missing resource (P1).
- Touch ergonomics: extra key row above keyboard (Tab, symbols, arrows, Esc, Ctrl), long-press drag handles for selection, pinch to change font size, optional keyboard shortcuts for external keyboards.
- Formatting: ktfmt/google-java-format on save (toggle).

**6.7 Build system (P0)**
- Build bar in the workspace: variant selector (debug/release plus flavors read from Gradle model), module selector, and actions: Assemble, Bundle (AAB), Clean, Install & Run, Sync.
- Structured build output: task list with durations, collapsible logs, error entries link to file and line.
- Signing: manage keystores (create, import, view fingerprint) stored in app-private encrypted storage. Release builds require selecting a signing config; debug uses an auto-generated debug keystore.
- Build cache and Gradle daemon settings; "Low memory mode" preset.
- Install: on the same device via `PackageInstaller` session (no root), or to another device via ADB Wi-Fi pairing UI (Android 11+ wireless debugging).
- Run configuration: launch activity choice, launch after install toggle.
- Output artifacts panel: APK/AAB list with size, variant, share and export actions.

**6.8 Terminal (P0)**
- Full xterm-256color emulator with pty, multiple sessions in tabs, session persistence while app alive, text selection and copy, font size control, hardware keyboard support.
- Environment preloaded: `PATH` includes JDK, SDK tools, Gradle, Git; `ANDROID_HOME`, `JAVA_HOME` set; `HOME` at `files/home`.
- Bundled shell: bash or a POSIX shell built for Android, plus coreutils subset, git, curl, tar, unzip, nano (or a minimal editor).
- "Open terminal here" from the project tree; build actions offer "Run in terminal" to expose the exact command.
- Package manager for extra CLI tools (P2).

**6.9 Testing (P0 for unit, P1 for instrumented)**
- Test explorer parses Gradle test XML reports: tree by module, class, method; status (Passed, Failed, Skipped) as text plus a small neutral status glyph; failure message and stack trace with clickable frames.
- Run all, run class, run single test via Gradle `--tests` filter. Re-run failed.
- Instrumented tests on the same device (P1): build test APK, install both, run via `am instrument`, stream results.
- Lint: run `lint<Variant>` and show findings in Problems panel with severity filters.

**6.10 Debugging and diagnostics (P1)**
- Logcat viewer filtered to the target package, with level filter, search, and pause. Log lines link to source when a stack trace is present.
- JDWP debugger: breakpoints, step, variables, using the on-device JDK's debugger tooling and `adb forward` to the app under test. Requires wireless debugging to itself (documented limitation) or a second device.
- Layout Inspector: out of scope v1.

**6.11 Version control (P1)**
- Git via bundled CLI with a UI layer: status, stage, commit, branch switch, pull, push, diff viewer (side-by-side on tablets, inline on phones), credential storage, SSH key management, `.gitignore` templates.

**6.12 Settings (P0)**
- Appearance: theme (System, Light, Dark), editor font family and size, line height, ligatures toggle.
- Editor: tabs vs spaces, auto-save, format on save, word wrap, keymap.
- Build: Gradle JVM args, parallel, offline mode, daemon idle timeout.
- Toolchain: manage components, storage usage, "Move projects" utility.
- Terminal: shell, font, bell, cursor style.
- Account: profile, sync toggle, connected identities, delete account.
- About: version, licenses (OSS notices required), privacy policy, onboarding replay.

**6.13 Account sync (P1)**
- Sync editor and build preferences, keymaps, recent project metadata (name, remote URL, last opened) and snippet library. No source files.
- Conflict policy: last write wins with timestamp, per-key.

**6.14 Documentation and help (P1)**
- Offline quick reference for Gradle tasks and common errors; "Explain this error" links to a curated troubleshooting page (no cloud AI in v1).

---

#### 7. UX and design system

**Design principles**
1. Quiet surfaces, one accent. Color communicates state and focus, never decoration.
2. Type carries hierarchy. Serif for display and page titles, humanist sans for UI, monospace for code.
3. No emojis, no gradients, no neon, no glowing or colored text pills. Status is expressed in words with neutral or accent color, and thin outlined chips only for filters.
4. Generous spacing on an 8 dp grid; large touch targets (48 dp minimum).
5. Everything reachable one-handed: primary actions in the bottom 40% of the screen.

**Color palette (inspired by the Claude Android app; final hex values to be confirmed by design)**

| Token | Light | Dark | Use |
|---|---|---|---|
| `surface.background` | #F5F4EF (ivory) | #1F1E1D (charcoal) | App background |
| `surface.elevated` | #FFFFFF | #2B2A27 | Cards, sheets, editor gutter |
| `surface.subtle` | #ECEAE3 | #33312E | Dividers, input backgrounds |
| `text.primary` | #1F1E1D | #F5F4EF | Headings, body |
| `text.secondary` | #6B6860 | #A8A49B | Captions, metadata |
| `accent.primary` | #C96442 (terracotta/clay) | #D97757 | Primary buttons, active states, cursor, links |
| `accent.onPrimary` | #FFFFFF | #1F1E1D | Text on accent |
| `state.error` | #B3261E | #E5776A | Errors, failed tests (text and thin left rule only) |
| `state.success` | #4F7A5B | #86B096 | Passed tests, verified labels |
| `state.warning` | #9A6B1B | #D2A24C | Warnings |

Rules: accent appears in at most one primary control per screen. No colored fills behind body text. Editor syntax theme derived from the same palette (muted, low-saturation tokens).

**Typography**
- Display and headings: a serif with warm character (open-source candidates: Source Serif 4, Fraunces at low optical size, or a licensed serif chosen by design). Sizes: Display 32/40, Title 24/32, Subtitle 18/26.
- UI text: Inter or Söhne-like grotesk (Inter is the open-source default). Body 16/24, Label 14/20, Caption 12/16. Medium weight for emphasis; never all caps except code.
- Code: JetBrains Mono, 13 sp default, ligatures off by default.
- Letter spacing neutral; no tracked-out uppercase labels.

**Shape and elevation**
- Corner radius: 12 dp cards, 10 dp buttons, 8 dp inputs, 24 dp bottom sheets. Fully rounded pills only for the FAB-equivalent "Run" button.
- Elevation expressed by surface tone change, not shadows (max 1 dp shadow for dialogs).
- Icons: single-weight outlined icon set (Material Symbols Outlined or Lucide), 24 dp, monochrome. No filled colorful icons.

**Motion**
- Durations 150 to 300 ms, standard decelerate easing. Shared-axis transitions between screens, fade-through for tab changes. No bounces, no confetti, no looping decorative animation.

**Layout patterns**
- Phone: bottom navigation with four destinations in the workspace: Files, Editor, Build, Terminal. Problems and Tests open as bottom sheets or full-screen when expanded.
- Tablet / foldable: three-pane: tree, editor, tool window; terminal docked bottom.
- Empty states use a serif heading, one sentence, one action. No illustrations with color fills.

**Accessibility**
- WCAG AA contrast for all text; dynamic type support up to 200% in UI text (editor font controlled separately); TalkBack labels on all controls; reduced motion setting honored.

**Content style**
- Sentence case everywhere. Short, direct copy. Error messages state what happened and one next step.

---

#### 8. Backend design (Supabase free tier)

**Free tier constraints to design around**: 500 MB Postgres, 1 GB storage, 5 GB egress, 50k MAU, 2 projects, project pauses after 7 days of inactivity (add a scheduled ping via GitHub Actions to prevent pause), 500k edge function invocations.

**Auth**
- Providers: Google (Android client ID plus web client ID for token exchange), Email/Password with verification. Disable phone and magic-link-only login to keep flows simple.
- Deep link `forge://auth/callback` for web fallback.

**Tables (all with RLS `auth.uid() = user_id`)**
- `profiles(id uuid pk references auth.users, display_name, avatar_url, created_at)`
- `user_settings(user_id pk, settings jsonb, updated_at)`
- `recent_projects(id, user_id, name, remote_url, last_opened_at, agp_version)`
- `keymaps(id, user_id, name, bindings jsonb)`
- `snippets(id, user_id, language, title, body, updated_at)`
- `device_installs(id, user_id, device_model, os_version, toolchain_versions jsonb, last_seen_at)` for support diagnostics (opt-in)
- `toolchain_manifests(version, published_at, manifest jsonb, signature)` read-only public, served through a CDN-cached endpoint

**Edge functions**
- `delete-account`: cascades deletes and removes storage objects.
- `manifest-latest`: returns the signed toolchain manifest with cache headers.

**Storage**
- Bucket `avatars` (public read, owner write, 512 KB limit). Toolchain binaries are not hosted on Supabase; use GitHub Releases or a CDN to protect the 5 GB egress cap.

**Client**
- `supabase-kt` (Auth, Postgrest, Storage modules). Offline-first: local Room cache, sync on foreground with connectivity.

---

#### 9. Non-functional requirements

- **Performance**: cold start to Home under 1.5 s on a mid-range 2023 device; editor keystroke latency under 16 ms for files up to 5k lines; incremental debug build of the Empty Compose template under 90 s warm on an 8 GB device.
- **Reliability**: builds survive app backgrounding; crash-free sessions 99.5%+; toolchain integrity verified on each launch (fast hash of manifest, full verification on demand).
- **Security**: keystores and tokens in Android Keystore-backed encrypted storage; no analytics of source code; TLS pinning for manifest endpoint; supply-chain: reproducible toolchain builds with published checksums.
- **Privacy**: minimal analytics (opt-in), compliant with Play Data Safety; clear statement that projects never leave the device unless the user pushes to Git.
- **Offline**: all IDE features work offline after setup; Gradle offline mode toggle; dependency cache preserved across upgrades.
- **Compatibility**: Android 10 to latest; arm64-v8a required (armeabi-v7a unsupported with clear message); tablets and foldables with adaptive layouts.
- **Localization**: English at launch, string resources externalized; RTL-safe layouts.
- **App size**: base APK under 60 MB; toolchain downloaded post-install.

---

#### 10. Technology stack

- Kotlin, Jetpack Compose, Material 3 with a fully custom theme, Navigation Compose, Hilt, Room, WorkManager and Foreground Services, Coroutines/Flow.
- Editor: tree-sitter via JNI; LSP clients for Kotlin Language Server and Eclipse JDT LS (or a lighter custom indexer for v1 if memory profiling demands it).
- Terminal: JNI pty layer, terminal emulator core (Termux-derived, Apache 2.0 or compatible).
- Toolchain: OpenJDK 17 built for Android arm64, Android SDK components repackaged as arm64 binaries where Google ships only x86 (aapt2, etc.), Gradle LTS, Kotlin, optional NDK subset. All licenses reviewed (Android SDK license acceptance flow presented to the user in setup).
- Backend: Supabase Auth, Postgres, Edge Functions (Deno), Storage; GitHub Releases or Cloudflare R2 for binaries.
- CI/CD: GitLab CI building the app, running unit and screenshot tests, producing signed AAB; separate pipeline building and checksumming toolchain artifacts.

---

#### 11. Success metrics

- Setup completion rate (started to verified) above 70%.
- Time to first successful build after setup under 10 minutes median.
- Build success rate for template projects above 95%.
- Day-7 retention above 25% for users who completed one build.
- Auth conversion: at least 60% of users create an account (local-only mode remains available).
- Crash-free sessions above 99.5%; Play rating target 4.5+.

---

#### 12. Release plan

**Milestone 0 (4 weeks): Foundations** design system, splash, onboarding, auth, Supabase schema, toolchain manifest pipeline.
**Milestone 1 (8 weeks): Build loop** toolchain installer, project wizard, editor with highlighting, Gradle assemble debug, install on device, terminal.
**Milestone 2 (6 weeks): Quality** variants and signing, release builds and AAB, unit test explorer, Problems panel, Logcat, LSP completion for Kotlin/Java.
**Milestone 3 (4 weeks): Beta** Git UI, settings sync, tablet layouts, accessibility pass, Play policy review, closed beta of 500 users.
**v1.0 launch** followed by v1.1 (instrumented tests, debugger) and v1.2 (NDK CMake projects, XML preview).

---

#### 13. Risks and mitigations

| Risk | Impact | Mitigation |
|---|---|---|
| Play policy rejection for downloaded executables | Distribution blocked | Early policy consultation, alternative distribution channels, toolchain fetched only from our signed manifest |
| Memory pressure kills Gradle daemon | Builds fail on 4 GB devices | Low memory preset, single-worker builds, clear minimum spec, daemon restarts |
| Kotlin LSP too heavy for phones | Poor completion experience | Lazy start, fallback to lightweight indexer, feature flag per device RAM |
| Supabase project pauses on inactivity | Login outages | Scheduled keep-alive, monitoring, plan to upgrade when MAU grows |
| SDK license and redistribution constraints | Legal exposure | Present Google's SDK license in setup; download SDK components from Google's endpoints where required; only redistribute rebuilt open-source binaries |
| Thermal throttling during long builds | Slow, failed builds | Thermal API monitoring, pause/resume, user guidance |

---

#### 14. Open questions

1. Editor engine: adopt an existing Android editor component or build in Compose for full theme control? (Recommend prototype both in Milestone 0.)
2. Kotlin language intelligence: full LSP vs. Kotlin Analysis API-based in-process indexer.
3. Should local-only mode be allowed to skip Welcome entirely, or require the Welcome screen once?
4. Default toolchain versions and update cadence (align with AGP LTS every 6 months?).
5. Monetization: free with optional paid tier (cloud builds, more sync) or fully free? Affects Supabase plan timing.

---
