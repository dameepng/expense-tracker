package com.example.expense_tracker.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.circle
import androidx.graphics.shapes.pill
import androidx.graphics.shapes.star
import com.example.expense_tracker.ui.theme.MaterialMotionTokens
import kotlin.math.min

/**
 * Material 3 Expressive Loading Indicator Defaults and Shapes
 * Specification: https://m3.material.io/components/loading-indicator/overview
 */
object MaterialLoadingIndicatorDefaults {
    val IndicatorSize: Dp = 48.dp
    val ContainedContainerSize: Dp = 56.dp
    val ContainedIndicatorSize: Dp = 36.dp
    val ContainerShape: Shape = RoundedCornerShape(16.dp)

    /**
     * Canonical Material 3 Expressive Polygons for Indeterminate Loading
     */
    val IndeterminatePolygons: List<RoundedPolygon> by lazy {
        listOf(
            // 1. Clover / 4-sided Cookie
            RoundedPolygon.star(
                numVerticesPerRadius = 4,
                radius = 1f,
                innerRadius = 0.65f,
                rounding = CornerRounding(0.35f, 0.5f),
                innerRounding = CornerRounding(0.35f, 0.5f)
            ),
            // 2. Sunny / Soft Burst (8-pointed)
            RoundedPolygon.star(
                numVerticesPerRadius = 8,
                radius = 1f,
                innerRadius = 0.78f,
                rounding = CornerRounding(0.3f, 0.5f),
                innerRounding = CornerRounding(0.3f, 0.5f)
            ),
            // 3. Rounded Pentagon
            RoundedPolygon.star(
                numVerticesPerRadius = 5,
                radius = 1f,
                innerRadius = 0.90f,
                rounding = CornerRounding(0.35f, 0.5f)
            ),
            // 4. Pill / Capsule
            RoundedPolygon.pill(
                width = 2.0f,
                height = 1.25f,
                smoothing = 0.45f
            ),
            // 5. Circle
            RoundedPolygon.circle(
                numVertices = 16
            )
        )
    }

    /**
     * Creates pre-calculated cyclic Morphs between adjacent polygons
     */
    fun createMorphs(polygons: List<RoundedPolygon> = IndeterminatePolygons): List<Morph> {
        if (polygons.size < 2) return emptyList()
        val morphs = ArrayList<Morph>(polygons.size)
        for (i in polygons.indices) {
            val next = (i + 1) % polygons.size
            morphs.add(Morph(polygons[i], polygons[next]))
        }
        return morphs
    }
}

/**
 * Uncontained Material 3 Expressive Loading Indicator.
 *
 * Morphs dynamically through expressive shapes with smooth rotation.
 * Follows the official M3 Loading Indicator specification:
 * https://m3.material.io/components/loading-indicator/overview
 */
@Composable
fun MaterialLoadingIndicator(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    size: Dp = MaterialLoadingIndicatorDefaults.IndicatorSize,
    polygons: List<RoundedPolygon> = MaterialLoadingIndicatorDefaults.IndeterminatePolygons
) {
    val morphs = remember(polygons) {
        MaterialLoadingIndicatorDefaults.createMorphs(polygons)
    }
    if (morphs.isEmpty()) return

    val morphCount = morphs.size
    val totalDurationMs = morphCount * 650

    val infiniteTransition = rememberInfiniteTransition(label = "m3_loading_indicator")

    // Continuous progress spanning across all morphs [0f, morphCount.toFloat())
    val animatedProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = morphCount.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = totalDurationMs, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "m3_loading_morph_progress"
    )

    // Continuous smooth rotation (90 degrees per shape transition)
    val animatedRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 90f * morphCount,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = totalDurationMs, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "m3_loading_rotation"
    )

    // Reusable Path to avoid allocations per frame
    val reusablePath = remember { Path() }

    Canvas(
        modifier = modifier
            .size(size)
            .semantics {
                progressBarRangeInfo = ProgressBarRangeInfo.Indeterminate
                contentDescription = "Loading"
            }
    ) {
        val totalProgress = animatedProgress
        val currentIndex = (totalProgress.toInt() % morphCount).coerceIn(0, morphCount - 1)
        val rawFraction = (totalProgress - totalProgress.toInt()).coerceIn(0f, 1f)

        // Apply M3 Emphasized easing to the morph transition
        val easedFraction = MaterialMotionTokens.Emphasized.transform(rawFraction)

        val activeMorph = morphs[currentIndex]

        // Reconstruct path from morph cubics
        reusablePath.reset()
        var isFirst = true
        activeMorph.forEachCubic(easedFraction) { cubic ->
            if (isFirst) {
                reusablePath.moveTo(cubic.anchor0X, cubic.anchor0Y)
                isFirst = false
            }
            reusablePath.cubicTo(
                cubic.control0X, cubic.control0Y,
                cubic.control1X, cubic.control1Y,
                cubic.anchor1X, cubic.anchor1Y
            )
        }
        reusablePath.close()

        val canvasWidth = this.size.width
        val canvasHeight = this.size.height
        val minDimension = min(canvasWidth, canvasHeight)

        // The canonical polygon coordinates lie within [-1, 1], diameter is 2.
        // Scale factor: leave a gentle padding margin (approx 85% of half-dimension).
        val scale = (minDimension / 2f) * 0.86f
        val centerOffset = Offset(canvasWidth / 2f, canvasHeight / 2f)

        withTransform({
            translate(left = centerOffset.x, top = centerOffset.y)
            rotate(degrees = animatedRotation, pivot = Offset.Zero)
            scale(scaleX = scale, scaleY = scale, pivot = Offset.Zero)
        }) {
            drawPath(
                path = reusablePath,
                color = color,
                style = Fill
            )
        }
    }
}

/**
 * Contained Material 3 Expressive Loading Indicator.
 *
 * Places the expressive shape-morphing loading indicator inside a styled container surface.
 * Follows the official M3 Loading Indicator specification:
 * https://m3.material.io/components/loading-indicator/overview
 */
@Composable
fun MaterialContainedLoadingIndicator(
    modifier: Modifier = Modifier,
    containerSize: Dp = MaterialLoadingIndicatorDefaults.ContainedContainerSize,
    indicatorSize: Dp = MaterialLoadingIndicatorDefaults.ContainedIndicatorSize,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    indicatorColor: Color = MaterialTheme.colorScheme.primary,
    containerShape: Shape = MaterialLoadingIndicatorDefaults.ContainerShape,
    tonalElevation: Dp = 2.dp,
    polygons: List<RoundedPolygon> = MaterialLoadingIndicatorDefaults.IndeterminatePolygons
) {
    Surface(
        modifier = modifier.size(containerSize),
        shape = containerShape,
        color = containerColor,
        tonalElevation = tonalElevation
    ) {
        Box(
            modifier = Modifier.size(containerSize),
            contentAlignment = Alignment.Center
        ) {
            MaterialLoadingIndicator(
                modifier = Modifier.size(indicatorSize),
                color = indicatorColor,
                size = indicatorSize,
                polygons = polygons
            )
        }
    }
}
