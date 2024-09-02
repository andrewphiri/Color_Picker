package com.drew.and.colorpicker

import androidx.compose.ui.graphics.Color
import androidx.core.graphics.ColorUtils
import junit.framework.TestCase.assertEquals
import org.junit.Test

class ColorUtilityTests {

    @Test
    fun findColor() {
        val colorName = findClosestColor(Color.White.toIntArray())

        assertEquals("100% White", colorName)
    }
}