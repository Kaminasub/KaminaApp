package com.kamina.app

import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp

// Define a reusable modifier for global margin
fun Modifier.globalMargin(left: Dp = 0.dp, right: Dp = 0.dp): Modifier {
    return this.padding(start = left, end = right)
}
