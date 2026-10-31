package ir.gahshomar.parsi.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/** شکل طاق ایرانی (قوس شاخ‌بزی) */
class ArchShape(private val archRatio: Float = 0.42f) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val w = size.width
        val h = size.height
        val spring = h * (1f - archRatio)
        val path = Path()
        path.moveTo(0f, h)
        path.lineTo(0f, spring)
        path.cubicTo(0f, spring - h * 0.30f, w * 0.5f, h * 0.06f, w * 0.5f, 0f)
        path.cubicTo(w * 0.5f, h * 0.06f, w, spring - h * 0.30f, w, spring)
        path.lineTo(w, h)
        path.close()
        return Outline.Generic(path)
    }
}

/** ستارهٔ هشت‌پر ایرانی */
fun starPath(center: Offset, outerRadius: Float, innerRadius: Float, points: Int = 8): Path {
    val path = Path()
    val step = PI / points
    var angle = -PI / 2
    for (i in 0 until points * 2) {
        val r = if (i % 2 == 0) outerRadius else innerRadius
        val x = center.x + (r * cos(angle)).toFloat()
        val y = center.y + (r * sin(angle)).toFloat()
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        angle += step
    }
    path.close()
    return path
}

/** نقش‌مایهٔ ستاره + گل میانی */
@Composable
fun StarMotif(modifier: Modifier = Modifier, color: Color = LocalGahPalette.current.gold) {
    Canvas(modifier = modifier) {
        val c = Offset(size.width / 2f, size.height / 2f)
        val r = size.minDimension / 2f
        drawPath(starPath(c, r, r * 0.42f, 8), color)
        drawCircle(color = color, radius = r * 0.18f, center = c, style = Stroke(width = r * 0.1f))
    }
}

/** حلقهٔ تذهیب با ستاره‌های ریز */
@Composable
fun Rosette(modifier: Modifier = Modifier, color: Color = LocalGahPalette.current.gold, petals: Int = 12) {
    Canvas(modifier = modifier) {
        val c = Offset(size.width / 2f, size.height / 2f)
        val r = size.minDimension / 2f
        drawCircle(color = color.copy(alpha = 0.25f), radius = r * 0.92f, center = c, style = Stroke(width = r * 0.06f))
        for (i in 0 until petals) {
            val angle = (2 * PI * i / petals)
            val px = c.x + (r * 0.72f * cos(angle)).toFloat()
            val py = c.y + (r * 0.72f * sin(angle)).toFloat()
            drawPath(starPath(Offset(px, py), r * 0.12f, r * 0.05f, 8), color.copy(alpha = 0.7f))
        }
        drawPath(starPath(c, r * 0.5f, r * 0.2f, 8), color.copy(alpha = 0.9f))
    }
}

/** زمینهٔ هندسی گره‌چینی (تکرار ستاره‌های هشت‌پر) */
@Composable
fun GirihBackground(
    modifier: Modifier = Modifier,
    color: Color = LocalGahPalette.current.motif,
    tile: Dp = 54.dp
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        drawGirih(color, tile.toPx())
    }
}

private fun DrawScope.drawGirih(color: Color, tile: Float) {
    var y = 0f
    var row = 0
    while (y < size.height + tile) {
        var x = if (row % 2 == 0) 0f else tile / 2f
        while (x < size.width + tile) {
            drawPath(starPath(Offset(x, y), tile * 0.30f, tile * 0.13f, 8), color)
            drawCircle(color = color, radius = tile * 0.05f, center = Offset(x + tile / 2f, y), style = Stroke(width = tile * 0.015f))
            x += tile
        }
        y += tile * 0.87f
        row++
    }
}

/** سربرگ طاق‌مانند با نقش و نگار */
@Composable
fun ArchHeader(
    modifier: Modifier = Modifier,
    height: Dp = 190.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val palette = LocalGahPalette.current
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(ArchShape())
            .background(
                Brush.verticalGradient(
                    listOf(palette.headerTop, palette.headerBottom)
                )
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            // کمان‌های تزئینی
            val rings = 3
            for (i in 0 until rings) {
                val inset = (i + 1) * (w * 0.09f)
                drawPath(
                    archPath(inset, inset * 0.5f, w - inset, h), palette.gold.copy(alpha = 0.13f)
                , style = Stroke(width = 2f))
            }
            // ستاره‌های گوشه
            drawPath(starPath(Offset(w * 0.09f, h * 0.32f), h * 0.05f, h * 0.021f, 8), palette.gold.copy(alpha = 0.5f))
            drawPath(starPath(Offset(w * 0.91f, h * 0.32f), h * 0.05f, h * 0.021f, 8), palette.gold.copy(alpha = 0.5f))
            // نوار پایین
            drawRect(
                color = palette.gold.copy(alpha = 0.22f),
                topLeft = Offset(0f, h - 6f),
                size = Size(w, 3f)
            )
        }
        Box(modifier = Modifier.fillMaxSize(), content = content)
    }
}

private fun archPath(left: Float, top: Float, right: Float, bottom: Float): Path {
    val path = Path()
    val width = right - left
    val springLine = top + width * 0.45f
    path.moveTo(left, bottom)
    path.lineTo(left, springLine)
    path.cubicTo(
        left, springLine - width * 0.32f,
        left + width * 0.5f, top,
        left + width * 0.5f, top
    )
    path.cubicTo(
        left + width * 0.5f, top,
        right, springLine - width * 0.32f,
        right, springLine
    )
    path.lineTo(right, bottom)
    path.close()
    return path
}

/** جداکنندهٔ تزئینی */
@Composable
fun OrnamentDivider(
    modifier: Modifier = Modifier,
    color: Color = LocalGahPalette.current.gold,
    thickness: Dp = 1.dp
) {
    Canvas(modifier = modifier.fillMaxWidth().height(16.dp)) {
        val cy = size.height / 2f
        val gap = size.width * 0.06f
        drawLine(color.copy(alpha = 0.45f), Offset(0f, cy), Offset(size.width / 2f - gap, cy), strokeWidth = thickness.toPx())
        drawLine(
            color.copy(alpha = 0.45f),
            Offset(size.width / 2f + gap, cy),
            Offset(size.width, cy),
            strokeWidth = thickness.toPx()
        )
        drawPath(starPath(Offset(size.width / 2f, cy), size.height * 0.42f, size.height * 0.18f, 8), color)
    }
}

/** نشان دایره‌ای */
@Composable
fun SealBadge(
    modifier: Modifier = Modifier,
    ring: Color = LocalGahPalette.current.gold,
    background: Color = MaterialTheme.colorScheme.primary,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .size(74.dp)
            .clip(CircleShape)
            .background(background),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(ring, radius = size.minDimension / 2f - 2f, style = Stroke(width = 2.5f))
            drawCircle(ring.copy(alpha = 0.55f), radius = size.minDimension / 2f - 9f, style = Stroke(width = 1f))
        }
        Box(modifier = Modifier.padding(6.dp), contentAlignment = Alignment.Center, content = content)
    }
}

/** خط جداکنندهٔ ساده با نقش مرکزی */
@Composable
fun SoftDivider(color: Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)) {
    Canvas(modifier = Modifier.fillMaxWidth().height(1.dp)) {
        drawLine(color, Offset(0f, 0f), Offset(size.width, 0f), strokeWidth = 1f)
    }
}

/** نماد شعلهٔ آتش (برای دین زرتشتی) */
@Composable
fun FlameIcon(modifier: Modifier = Modifier, color: Color = LocalGahPalette.current.gold) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val outer = Path().apply {
            moveTo(w * 0.5f, 0f)
            cubicTo(w * 0.62f, h * 0.22f, w * 0.9f, h * 0.42f, w * 0.9f, h * 0.66f)
            cubicTo(w * 0.9f, h * 0.87f, w * 0.72f, h, w * 0.5f, h)
            cubicTo(w * 0.28f, h, w * 0.1f, h * 0.87f, w * 0.1f, h * 0.66f)
            cubicTo(w * 0.1f, h * 0.44f, w * 0.3f, h * 0.34f, w * 0.38f, h * 0.14f)
            cubicTo(w * 0.42f, h * 0.24f, w * 0.44f, h * 0.3f, w * 0.5f, h * 0.34f)
            cubicTo(w * 0.52f, h * 0.22f, w * 0.52f, h * 0.1f, w * 0.5f, 0f)
            close()
        }
        drawPath(outer, color)
        val inner = Path().apply {
            moveTo(w * 0.5f, h * 0.38f)
            cubicTo(w * 0.62f, h * 0.55f, w * 0.72f, h * 0.62f, w * 0.72f, h * 0.74f)
            cubicTo(w * 0.72f, h * 0.86f, w * 0.63f, h * 0.94f, w * 0.5f, h * 0.94f)
            cubicTo(w * 0.37f, h * 0.94f, w * 0.28f, h * 0.86f, w * 0.28f, h * 0.74f)
            cubicTo(w * 0.28f, h * 0.63f, w * 0.4f, h * 0.56f, w * 0.5f, h * 0.38f)
            close()
        }
        drawPath(inner, Color.White.copy(alpha = 0.75f))
    }
}
