package com.anoy.ide

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.anoy.ide.core.session.SessionStore
import com.anoy.ide.core.session.ThemePreference
import com.anoy.ide.core.ui.theme.ForgeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        splashScreen.setKeepOnScreenCondition {
            false
        }

        val sessionStore = SessionStore(applicationContext)

        setContent {
            val themeName by sessionStore.theme.collectAsState(
                initial = ThemePreference.SYSTEM.name
            )
            val preference = ThemePreference.from(themeName)
            ForgeTheme(darkTheme = preference.usesDarkTheme()) {
                ForgeApp(sessionStore = sessionStore)
            }
        }
    }
}
