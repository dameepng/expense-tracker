package com.example.expense_tracker.ui

import androidx.compose.ui.graphics.Path
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.circle
import androidx.graphics.shapes.star
import com.example.expense_tracker.ui.components.MaterialLoadingIndicatorDefaults
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class LoadingIndicatorTest {

    private fun Morph.toComposePath(progress: Float, path: Path = Path()): Path {
        path.reset()
        var first = true
        forEachCubic(progress) { cubic ->
            if (first) {
                path.moveTo(cubic.anchor0X, cubic.anchor0Y)
                first = false
            }
            path.cubicTo(
                cubic.control0X, cubic.control0Y,
                cubic.control1X, cubic.control1Y,
                cubic.anchor1X, cubic.anchor1Y
            )
        }
        path.close()
        return path
    }

    @Test
    fun testMorphToComposePath() {
        val circle = RoundedPolygon.circle()
        val star4 = RoundedPolygon.star(
            numVerticesPerRadius = 4,
            radius = 1f,
            innerRadius = 0.65f,
            rounding = CornerRounding(0.35f, 0.5f),
            innerRounding = CornerRounding(0.35f, 0.5f)
        )

        val morph = Morph(circle, star4)
        val path = morph.toComposePath(0.5f)

        assertNotNull(path)
        assertFalse(path.isEmpty)
    }

    @Test
    fun testMaterialLoadingIndicatorDefaults() {
        val polygons = MaterialLoadingIndicatorDefaults.IndeterminatePolygons
        assertEquals(5, polygons.size)

        val morphs = MaterialLoadingIndicatorDefaults.createMorphs(polygons)
        assertEquals(5, morphs.size)

        val reusablePath = Path()
        val fractions = floatArrayOf(0f, 0.25f, 0.5f, 0.75f, 1f)

        for (morph in morphs) {
            for (fraction in fractions) {
                morph.toComposePath(fraction, reusablePath)
                assertFalse("Path must not be empty for fraction $fraction", reusablePath.isEmpty)
            }
        }
    }
}
