package com.example.util

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import kotlin.math.abs

enum class ReaderGesture {
    DOUBLE_TAP,
    LONG_PRESS,
    SWIPE_UP,
    SWIPE_DOWN,
    SWIPE_LEFT,
    SWIPE_RIGHT,
    TWO_FINGER_TAP,
    THREE_FINGER_TAP
}

fun Modifier.readerGestures(
    vararg keys: Any?,
    onTap: (androidx.compose.ui.input.pointer.PointerInputScope.(Offset) -> Unit)? = null,
    onGesture: (ReaderGesture) -> Unit
): Modifier = this.pointerInput(*keys) {
    awaitEachGesture {
        var event = awaitPointerEvent(PointerEventPass.Initial)
        val initialPointers = event.changes.size
        
        // Count maximum pointers during the gesture
        var maxPointers = initialPointers
        
        // Initial down
        var totalPan = Offset.Zero
        val downTime = event.changes.first().uptimeMillis
        
        while (event.changes.any { it.pressed }) {
            event = awaitPointerEvent(PointerEventPass.Initial)
            if (event.changes.size > maxPointers) {
                maxPointers = event.changes.size
            }
            // Accumulate movement
            val pan = event.calculatePan()
            totalPan += pan
        }
        
        val upTime = event.changes.first().uptimeMillis
        val duration = upTime - downTime
        
        val isSwipe = totalPan.getDistance() > 100f // Threshold for swipe
        
        if (isSwipe) {
            if (abs(totalPan.x) > abs(totalPan.y)) {
                if (totalPan.x > 0) {
                    onGesture(ReaderGesture.SWIPE_RIGHT)
                } else {
                    onGesture(ReaderGesture.SWIPE_LEFT)
                }
            } else {
                if (totalPan.y > 0) {
                    onGesture(ReaderGesture.SWIPE_DOWN)
                } else {
                    onGesture(ReaderGesture.SWIPE_UP)
                }
            }
        } else {
            if (maxPointers == 3) {
                onGesture(ReaderGesture.THREE_FINGER_TAP)
            } else if (maxPointers == 2) {
                onGesture(ReaderGesture.TWO_FINGER_TAP)
            }
        }
    }
}.pointerInput(*keys) {
    detectTapGestures(
        onTap = { offset -> onTap?.invoke(this, offset) },
        onDoubleTap = { onGesture(ReaderGesture.DOUBLE_TAP) },
        onLongPress = { onGesture(ReaderGesture.LONG_PRESS) }
    )
}

// Extension function helper to calculate pan manually since it's not available in this scope directly
fun PointerEvent.calculatePan(): Offset {
    var pan = Offset.Zero
    for (change in changes) {
        if (change.pressed && change.previousPressed) {
            pan += change.position - change.previousPosition
        }
    }
    return if (changes.isNotEmpty()) pan / changes.size.toFloat() else Offset.Zero
}
