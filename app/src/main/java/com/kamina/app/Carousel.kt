package com.kamina.app

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.kamina.app.api.EntityResponse
import androidx.compose.ui.draw.clip

@Composable
fun CarouselItem(entity: EntityResponse, nextEntity: EntityResponse?) {
    Box(
        modifier = Modifier
            .width(400.dp)
            .padding(end = 10.dp) // This adds padding on both left and right
    ) {
        // Display entity image with rounded corners
        entity.pic?.let {
            Image(
                painter = rememberAsyncImagePainter(it),
                contentDescription = "Entity Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .padding(horizontal = 10.dp)
                    .clip(RoundedCornerShape(12.dp))  // Apply 12dp radius to the image
                    .background(Color.White),  // Optional background if needed
                contentScale = ContentScale.Crop  // Crop the image to fit the container without distortion
            )
        }

        // Bottom-left alignment for the logo or name
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp) // Padding from the left and bottom
        ) {
            if (entity.logo != null && entity.logo.isNotEmpty()) {
                // Display entity logo at the bottom-left
                Image(
                    painter = rememberAsyncImagePainter(entity.logo),
                    contentDescription = "Entity Logo",
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(8.dp)) // Optional: Apply rounded corners to the logo
                )
            } else {
                // Display entity name at the bottom-left if the logo is not available
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
