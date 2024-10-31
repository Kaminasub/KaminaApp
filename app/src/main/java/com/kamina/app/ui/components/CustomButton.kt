package com.kamina.app.ui.components

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
fun CustomButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    buttonColor: Color = Color(0xFF867E8D).copy(alpha = 0.3f), // Default button color
    hoverColor: Color = Color(0xFF9927FF), // Hover color
    textColor: Color = Color(0xFFFFFFFF),
    buttonHeight: Dp = 40.dp,
    buttonWidth: Dp = 200.dp,
    textSize: TextUnit = 16.sp
) {
    // State to manage hover effect
    var isHovered by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isHovered) hoverColor else buttonColor // Change color on hover
        ),
        modifier = modifier
            .height(buttonHeight)
            .width(buttonWidth)
            .pointerInput(Unit) {
                coroutineScope.launch {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            isHovered = event.changes.any { it.pressed }
                        }
                    }
                }
            },
        shape = RoundedCornerShape(24.dp) // Optional: Add rounded corners to the button
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = textSize
        )
    }
}