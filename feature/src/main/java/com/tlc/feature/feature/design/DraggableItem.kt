package com.tlc.feature.feature.design

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.tlc.domain.model.firebase.DesignItem

@Composable
fun DraggableItem(
    item: DesignItem,
    boundary: Rect,
    onPositionChange: (Offset) -> Unit
) {
    var offset by remember { mutableStateOf(Offset(item.xPosition, item.yPosition)) }

    Box(
        modifier = Modifier
            .offset { IntOffset(offset.x.toInt(), offset.y.toInt()) }
            .size(50.dp)
            .background(if (item.type == "TABLE") Color.Blue else Color.Red)
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consumeAllChanges()

                    val newX = offset.x + dragAmount.x
                    val newY = offset.y + dragAmount.y

                    // Constrain within boundary
                    val constrainedX = newX.coerceIn(
                        boundary.left,
                        boundary.right - 50.dp.toPx()
                    )
                    val constrainedY = newY.coerceIn(
                        boundary.top,
                        boundary.bottom - 50.dp.toPx()
                    )

                    offset = Offset(constrainedX, constrainedY)
                    onPositionChange(offset)
                }
            }
    )
}
