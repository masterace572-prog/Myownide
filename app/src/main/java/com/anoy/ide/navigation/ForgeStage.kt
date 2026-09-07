package com.anoy.ide.navigation

/**
 * High-level session stages that drive the first-launch and authenticated flow.
 *
 * TODO(M0): replace with a persisted session/setup state backed by DataStore and
 * Supabase auth once the account layer is wired up.
 */
enum class ForgeStage {
    ONBOARDING,
    WELCOME,
    AUTH,
    TOOLCHAIN_SETUP,
    WORKSPACE
}
