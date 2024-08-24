package com.tlc.feature.feature.design

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
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
    var offsetX by remember { mutableStateOf(item.xPosition) }
    var offsetY by remember { mutableStateOf(item.yPosition) }

    Box(
        modifier = Modifier
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .size(50.dp)
            .background(if (item.type == "TABLE") Color.Blue else Color.Red)
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    offsetX += dragAmount.x
                    offsetY += dragAmount.y
                    onPositionChange(Offset(offsetX, offsetY))
                }
            }
    )
}