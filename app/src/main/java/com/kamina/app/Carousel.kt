package com.kamina.app

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.kamina.app.api.EntityResponse

@Composable
fun CarouselItem(entity: EntityResponse, nextEntity: EntityResponse?) {
    Box(
        modifier = Modifier
            .width(400.dp)
            .background(Color.LightGray.copy(alpha = 0.3f)) // Add background color with some transparency
    ) {
        // Display entity image
        entity.pic?.let {
            Image(
                painter = rememberAsyncImagePainter(it),
                contentDescription = "Entity Image",
                modifier = Modifier
                    .fillMaxWidth()  // Dynamically fill available width
                    .padding(horizontal = 10.dp)  // Use padding for the margins
                    .height(250.dp)  // Set the fixed height
            )
        }

        // Bottom-left alignment for the logo or name
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                  // Padding to avoid touching the image edges
        ) {
            if (entity.logo != null && entity.logo.isNotEmpty()) {
                // Display entity logo at the bottom-left
                Image(
                    painter = rememberAsyncImagePainter(entity.logo),
                    contentDescription = "Entity Logo",
                    modifier = Modifier
                        .size(100.dp)  // Adjust size as needed
                        .globalMargin(left = 10.dp)
                )
            } else {
                // Display entity name at the bottom-left if logo is not available
                Text(
                    text = entity.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }

    // Pre-fetch the next entity's image if available
    nextEntity?.let {
        rememberAsyncImagePainter(it.pic)
    }
}
