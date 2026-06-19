package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.util.AnnotationPoint
import com.example.util.PdfAnnotation

enum class AnnotationTool { NONE, PEN, HIGHLIGHT, TEXT, ERASER }

@Composable
fun PdfAnnotationOverlay(
    page: Int,
    annotations: List<PdfAnnotation>,
    currentTool: AnnotationTool,
    onAnnotationAdded: (PdfAnnotation) -> Unit,
    onEraseRequested: (Float, Float) -> Unit,
    onTextRequested: (Float, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var currentPath by remember { mutableStateOf<List<Offset>?>(null) }
    var touchStart by remember { mutableStateOf<Offset?>(null) }
    var touchCurrent by remember { mutableStateOf<Offset?>(null) }

    Canvas(modifier = modifier
        .fillMaxSize()
        .pointerInput(currentTool) {
            if (currentTool == AnnotationTool.PEN || currentTool == AnnotationTool.HIGHLIGHT) {
                detectDragGestures(
                    onDragStart = { offset ->
                        if (currentTool == AnnotationTool.PEN) {
                            currentPath = listOf(offset)
                        } else if (currentTool == AnnotationTool.HIGHLIGHT) {
                            touchStart = offset
                            touchCurrent = offset
                        }
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        if (currentTool == AnnotationTool.PEN) {
                            currentPath = currentPath?.plus(change.position)
                        } else if (currentTool == AnnotationTool.HIGHLIGHT) {
                            touchCurrent = change.position
                        }
                    },
                    onDragEnd = {
                        if (currentTool == AnnotationTool.PEN && currentPath != null && currentPath!!.size > 1) {
                            val points = currentPath!!.map { AnnotationPoint(it.x / size.width, it.y / size.height) }
                            onAnnotationAdded(
                                PdfAnnotation(
                                    page = page,
                                    type = "PEN",
                                    points = points,
                                    color = "#FF0000"
                                )
                            )
                            currentPath = null
                        } else if (currentTool == AnnotationTool.HIGHLIGHT && touchStart != null && touchCurrent != null) {
                            val st = touchStart!!
                            val curr = touchCurrent!!
                            val x1 = minOf(st.x, curr.x) / size.width
                            val y1 = maxOf(minOf(st.y, curr.y) - 10, 0f) / size.height // Adjust height a bit
                            val x2 = maxOf(st.x, curr.x) / size.width
                            val y2 = minOf(maxOf(st.y, curr.y) + 10, size.height.toFloat()) / size.height
                            
                            val w = x2 - x1
                            val h = y2 - y1
                            
                            if (w > 0.01f && h > 0.01f) {
                                onAnnotationAdded(
                                    PdfAnnotation(
                                        page = page,
                                        type = "HIGHLIGHT",
                                        x = x1,
                                        y = y1,
                                        width = w,
                                        height = h,
                                        color = "#40FFFF00"
                                    )
                                )
                            }
                            touchStart = null
                            touchCurrent = null
                        }
                    },
                    onDragCancel = {
                        currentPath = null
                        touchStart = null
                        touchCurrent = null
                    }
                )
            }
        }
        .pointerInput(currentTool) {
            if (currentTool == AnnotationTool.TEXT || currentTool == AnnotationTool.ERASER) {
                detectTapGestures { offset ->
                    if (currentTool == AnnotationTool.TEXT) {
                        onTextRequested(offset.x / size.width, offset.y / size.height)
                    } else if (currentTool == AnnotationTool.ERASER) {
                        onEraseRequested(offset.x / size.width, offset.y / size.height)
                    }
                }
            }
        }
    ) {
        // Draw existing annotations
        annotations.filter { it.page == page }.forEach { ann ->
            when (ann.type) {
                "PEN" -> {
                    val path = Path()
                    ann.points?.forEachIndexed { index, p ->
                        val x = p.x * size.width
                        val y = p.y * size.height
                        if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    }
                    drawPath(path, color = Color(android.graphics.Color.parseColor(ann.color ?: "#000000")), style = Stroke(width = 3.dp.toPx()))
                }
                "HIGHLIGHT" -> {
                    drawRect(
                        color = Color(android.graphics.Color.parseColor(ann.color ?: "#40FFFF00")),
                        topLeft = Offset(ann.x * size.width, ann.y * size.height),
                        size = androidx.compose.ui.geometry.Size(ann.width * size.width, ann.height * size.height)
                    )
                }
                "TEXT" -> {
                    drawContext.canvas.nativeCanvas.drawText(
                        ann.text ?: "",
                        ann.x * size.width,
                        ann.y * size.height,
                        android.graphics.Paint().apply {
                            color = android.graphics.Color.parseColor(ann.color ?: "#000000")
                            textSize = 20.dp.toPx()
                            isAntiAlias = true
                        }
                    )
                }
            }
        }

        // Draw current path
        currentPath?.let { pathPts ->
            val path = Path()
            pathPts.forEachIndexed { index, p ->
                if (index == 0) path.moveTo(p.x, p.y) else path.lineTo(p.x, p.y)
            }
            drawPath(path, color = Color.Red, style = Stroke(width = 3.dp.toPx()))
        }

        // Draw current highlight
        if (touchStart != null && touchCurrent != null) {
            val st = touchStart!!
            val curr = touchCurrent!!
            drawRect(
                color = Color(0x40FFFF00),
                topLeft = Offset(minOf(st.x, curr.x), minOf(st.y, curr.y)),
                size = androidx.compose.ui.geometry.Size(kotlin.math.abs(curr.x - st.x), kotlin.math.abs(curr.y - st.y))
            )
        }
    }
}
