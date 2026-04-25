package com.datark.fortunewheel

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

private val WheelColors = listOf(
    Color(0xFFEF4444),
    Color(0xFFF97316),
    Color(0xFFEAB308),
    Color(0xFF22C55E),
    Color(0xFF06B6D4),
    Color(0xFF3B82F6),
    Color(0xFF8B5CF6),
    Color(0xFFEC4899)
)

@Composable
fun FortuneWheel(
    labels: List<String>,
    onSpinFinished: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val rotation = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val labelTextSizePx = with(density) { 16.dp.toPx() }
    val indicatorSizePx = with(density) { 28.dp.toPx() }
    val ringStrokePx = with(density) { 4.dp.toPx() }

    var lastReportedIndex by remember { mutableStateOf(-1) }

    LaunchedEffect(labels.size) {
        rotation.snapTo(((rotation.value % 360f) + 360f) % 360f)
        lastReportedIndex = -1
    }

    val gestureModifier = Modifier.pointerInput(labels.size) {
        awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Main)
            scope.launch { rotation.stop() }

            val cx = size.width / 2f
            val cy = size.height / 2f

            var lastAngle = angleDeg(down.position, cx, cy)
            var lastTime = down.uptimeMillis
            var velocityDegPerMs = 0f
            var target = rotation.value

            while (true) {
                val event = awaitPointerEvent(PointerEventPass.Main)
                val change = event.changes.firstOrNull { it.id == down.id }
                    ?: event.changes.first()

                val now = change.uptimeMillis
                val angle = angleDeg(change.position, cx, cy)
                val delta = shortestDelta(lastAngle, angle)
                val dt = (now - lastTime).coerceAtLeast(1L)

                val instantV = delta / dt.toFloat()
                velocityDegPerMs = velocityDegPerMs * 0.6f + instantV * 0.4f

                if (delta != 0f) {
                    target += delta
                    val snapTarget = target
                    scope.launch { rotation.snapTo(snapTarget) }
                    change.consume()
                }

                lastAngle = angle
                lastTime = now

                if (!change.pressed) break
            }

            val initialVDegPerSec = velocityDegPerMs * 1000f
            scope.launch {
                if (abs(initialVDegPerSec) > 60f) {
                    rotation.animateDecay(
                        initialVelocity = initialVDegPerSec,
                        animationSpec = exponentialDecay(frictionMultiplier = 0.6f)
                    )
                }
                val idx = winningIndex(rotation.value, labels.size)
                if (idx != lastReportedIndex) {
                    lastReportedIndex = idx
                    onSpinFinished(idx)
                }
            }
        }
    }

    Canvas(modifier = modifier.then(gestureModifier)) {
        val diameter = min(size.width, size.height)
        val radius = diameter / 2f
        val center = Offset(size.width / 2f, size.height / 2f)
        val wheelTopLeft = Offset(center.x - radius, center.y - radius)
        val wheelSize = Size(diameter, diameter)

        val n = labels.size.coerceAtLeast(1)
        val sweep = 360f / n

        rotate(rotation.value, pivot = center) {
            for (i in 0 until n) {
                val color = WheelColors[i % WheelColors.size]
                val startAngle = -90f + i * sweep - sweep / 2f
                drawArc(
                    color = color,
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter = true,
                    topLeft = wheelTopLeft,
                    size = wheelSize
                )
                drawArc(
                    color = Color.Black.copy(alpha = 0.25f),
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter = true,
                    topLeft = wheelTopLeft,
                    size = wheelSize,
                    style = Stroke(width = 2f)
                )
            }

            for (i in 0 until n) {
                val midAngleDeg = -90f + i * sweep
                val midAngleRad = (midAngleDeg * PI / 180.0).toFloat()
                val labelRadius = radius * 0.65f
                val tx = center.x + labelRadius * cos(midAngleRad)
                val ty = center.y + labelRadius * sin(midAngleRad)
                val canvas = drawContext.canvas.nativeCanvas

                val paint = android.graphics.Paint().apply {
                    color = android.graphics.Color.WHITE
                    textSize = labelTextSizePx
                    isAntiAlias = true
                    textAlign = android.graphics.Paint.Align.CENTER
                    setShadowLayer(4f, 0f, 0f, android.graphics.Color.BLACK)
                    isFakeBoldText = true
                }
                canvas.save()
                canvas.rotate(midAngleDeg + 90f, tx, ty)
                val maxWidth = radius * 0.55f
                val truncated = ellipsize(labels[i], paint, maxWidth)
                val baselineOffset = (paint.descent() + paint.ascent()) / 2f
                canvas.drawText(truncated, tx, ty - baselineOffset, paint)
                canvas.restore()
            }
        }

        drawCircle(
            color = Color(0xFF111827),
            radius = radius,
            center = center,
            style = Stroke(width = ringStrokePx)
        )
        drawCircle(
            color = Color(0xFF111827),
            radius = radius * 0.08f,
            center = center
        )
        drawCircle(
            color = Color.White,
            radius = radius * 0.04f,
            center = center
        )

        val pointerPath = Path().apply {
            val tipY = center.y - radius + indicatorSizePx * 0.55f
            val baseY = center.y - radius - indicatorSizePx * 0.35f
            moveTo(center.x, tipY)
            lineTo(center.x - indicatorSizePx / 2f, baseY)
            lineTo(center.x + indicatorSizePx / 2f, baseY)
            close()
        }
        drawPath(pointerPath, Color(0xFFDC2626))
        drawPath(pointerPath, Color.Black, style = Stroke(width = 2f))
    }
}

private fun angleDeg(p: Offset, cx: Float, cy: Float): Float {
    val rad = atan2((p.y - cy).toDouble(), (p.x - cx).toDouble())
    return Math.toDegrees(rad).toFloat()
}

private fun shortestDelta(from: Float, to: Float): Float {
    var d = to - from
    while (d > 180f) d -= 360f
    while (d < -180f) d += 360f
    return d
}

private fun winningIndex(rotationDeg: Float, n: Int): Int {
    if (n <= 0) return 0
    val sweep = 360f / n
    val raw = -rotationDeg / sweep
    val idx = ((raw % n) + n) % n
    return idx.toInt() % n
}

private fun ellipsize(text: String, paint: android.graphics.Paint, maxWidth: Float): String {
    if (paint.measureText(text) <= maxWidth) return text
    var s = text
    while (s.isNotEmpty() && paint.measureText("$s…") > maxWidth) {
        s = s.dropLast(1)
    }
    return if (s.isEmpty()) "…" else "$s…"
}
