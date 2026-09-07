package com.anoy.ide.core.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Shape system.
 *
 * 12 dp cards, 10 dp buttons, 8 dp inputs, 24 dp bottom sheets. The only
 * fully-rounded element in the app is the Run FAB-equivalent button.
 */
object ForgeShapes {
    val Shapes = Shapes(
        extraSmall = RoundedCornerShape(6.dp),
        small = RoundedCornerShape(8.dp),
        medium = RoundedCornerShape(10.dp),
        large = RoundedCornerShape(12.dp),
        extraLarge = RoundedCornerShape(24.dp)
    )
}
