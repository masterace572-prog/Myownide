# Forge IDE — Architecture

## Module layout

Single `:app` module for Milestone 0. Split into feature/UI/library modules as
the toolchain, editor and terminal grow.

```
app/src/main/java/com/anoy/ide/
├── core/ui/theme/        Design tokens (color, type, shape, theme)
├── core/ui/components/   Reusable quiet UI primitives
├── core/gradle/          Gradle wrapper resolution (project-specific Gradle)
├── navigation/           High-level session flow model
├── onboarding/           First-run onboarding
├── auth/                 Welcome + email/password flow
├── toolchain/            Toolchain setup list + simulated install state
├── project/              Project wizard, generator, manager, project tree, editor
├── workspace/            Main shell + Build tab (variant/assembly shell)
└── ForgeApp.kt            Top-level session router
```

## Design system rules

- One accent color per screen. Color communicates state, never decoration.
- Serif for display/headings, sans for UI, monospace for code.
- No emojis, gradients, neon, glowing elements, or colored text pills.
- Status is expressed in words, optionally with a small neutral glyph.
- 8 dp spacing grid, 48 dp minimum touch targets, 12/10/8/24 radius scale.

## Package / naming conventions

- Package segments: `core`, `feature`, `navigation`, `ui`.
- Screens end in `Screen`; state holders end in `ViewModel`.
- Public composables are `@Composable` and accept `modifier` when appropriate.

## Current data flow

`MainActivity` -> `ForgeTheme` -> `ForgeApp` (DataStore stage flow) -> feature
screens. New projects are created by `GradleProjectGenerator` under
`files/projects/<name>` and listed by `ProjectManager`.

## Roadmap handoff

See [`ROADMAP.md`](./ROADMAP.md) for the ordered task list and
[`COMPATIBILITY.md`](./COMPATIBILITY.md) for how build/opened projects use
their own Gradle wrapper so anything that builds in Android Studio also builds in
Forge.
