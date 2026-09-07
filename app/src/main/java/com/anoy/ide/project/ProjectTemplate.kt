package com.anoy.ide.project

/**
 * New-project templates mirroring the PRD section 6.5.
 */
enum class ProjectTemplate(
    val displayName: String,
    val description: String,
    val usesCompose: Boolean
) {
    EMPTY_COMPOSE_ACTIVITY(
        displayName = "Empty Compose Activity",
        description = "A minimal Jetpack Compose activity with Material 3.",
        usesCompose = true
    ),
    EMPTY_VIEWS_ACTIVITY(
        displayName = "Empty Views Activity",
        description = "A classic Views activity with a simple layout.",
        usesCompose = false
    ),
    BASIC_VIEWS_NAVIGATION(
        displayName = "Basic Views with Navigation",
        description = "Views with a second destination skeleton.",
        usesCompose = false
    ),
    LIBRARY_MODULE(
        displayName = "Library module",
        description = "A reusable Android library module, no activity.",
        usesCompose = false
    ),
    NO_ACTIVITY(
        displayName = "No Activity",
        description = "A plain app module without a launch activity.",
        usesCompose = false
    )
}
