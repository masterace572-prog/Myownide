package com.anoy.ide.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.anoy.ide.core.ui.components.ForgePrimaryButton
import com.anoy.ide.core.ui.components.ForgeTextButton
import kotlinx.coroutines.launch

private data class OnboardingPage(
    val title: String,
    val body: String
)

private val pages = listOf(
    OnboardingPage(
        title = "Build Android apps on Android.",
        body = "Forge gives you a complete, professional Android development loop that runs entirely on your phone or tablet."
    ),
    OnboardingPage(
        title = "A real toolchain, installed once.",
        body = "Forge downloads and verifies JDK, the Android SDK, Gradle and more. After one setup, you can build offline."
    ),
    OnboardingPage(
        title = "Your work stays on your device.",
        body = "Projects live locally. An account is only used for settings sync and entitlements, never for your source code."
    )
)

/**
 * Three-page onboarding with a large serif heading, two-line body, page dots and
 * a single monochrome framed illustration.
 */
@Composable
fun OnboardingScreen(onFinished: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val currentPage = pagerState.currentPage
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                ForgeTextButton(text = "Skip", onClick = onFinished)
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) { page ->
                val item = pages[page]
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 28.dp, vertical = 24.dp),
                    verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
                ) {
                    OnboardingIllustration(tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(40.dp))
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.displayMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = item.body,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            PageIndicator(current = currentPage, total = pages.size)

            ForgePrimaryButton(
                text = if (currentPage == pages.lastIndex) "Get started" else "Continue",
                onClick = {
                    if (currentPage == pages.lastIndex) {
                        onFinished()
                    } else {
                        scope.launch {
                            pagerState.animateScrollToPage(currentPage + 1)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp)
            )
        }
    }
}

@Composable
private fun PageIndicator(current: Int, total: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(total) { index ->
            val selected = index == current
            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .size(6.dp)
                    .background(
                        color = if (selected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outlineVariant
                        },
                        shape = RoundedCornerShape(50)
                    )
            )
        }
    }
}

/**
 * Placeholder for the monochrome line illustration. M0 uses a simple framed
 * glyph derived from the app mark to avoid shipping raster art.
 */
@Composable
private fun OnboardingIllustration(tint: Color) {
    Box(
        modifier = Modifier
            .size(120.dp)
            .background(
                color = tint.copy(alpha = 0.08f),
                shape = RoundedCornerShape(24.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.material3.Icon(
            imageVector = androidx.compose.material.icons.Icons.Outlined.Code,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(56.dp)
        )
    }
}
