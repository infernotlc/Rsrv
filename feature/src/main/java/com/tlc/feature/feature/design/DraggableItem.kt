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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.tlc.domain.model.firebase.DesignItem
import kotlin.math.roundToInt

@Composable
fun DraggableItem(
    item: DesignItem,
    onPositionChange: (Offset) -> Unit
) {
    var offset by remember { mutableStateOf(Offset(item.xPosition, item.yPosition)) }

    Box(
        modifier = Modifier
            .offset(offset.x.dp, offset.y.dp)
            .size(50.dp)  // Example size
            .background(if (item.type == "TABLE") Color.Blue else Color.Red) // Different color for Table and Chair
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consumeAllChanges()
                    val dragFactor = 0.3f // Adjust this factor to slow down the drag speed
                    offset = offset.copy(
                        x = offset.x + dragAmount.x * dragFactor,
                        y = offset.y + dragAmount.y * dragFactor
                    )
                    onPositionChange(offset)
                }
            }
    )
}
