package com.kamina.app.ui.components

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kamina.app.ConfigurationActivity
import com.kamina.app.HomePageActivity
import com.kamina.app.LoginActivity
import com.kamina.app.MoviesActivity
import com.kamina.app.SearchPageActivity
import com.kamina.app.SeriesActivity
import com.kamina.app.TvPageActivity

@Composable
fun UserDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    context: Context
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { onDismissRequest() },
        modifier = Modifier.width(150.dp)  // Increase width for better visibility on TV
    ) {
        DropdownMenuItem(
            onClick = {
                onDismissRequest()
                // Direct Intent to HomePageActivity
                val intent = Intent(context, HomePageActivity::class.java)
                context.startActivity(intent)
            },
            text = { Text("Home") }
        )
        DropdownMenuItem(
            onClick = {
                onDismissRequest()
                val intent = Intent(context, SeriesActivity::class.java)
                context.startActivity(intent)
                (context as? Activity)?.finishAffinity()
            },
            text = { Text("Series") }
        )
        DropdownMenuItem(
            onClick = {
                onDismissRequest()
                val intent = Intent(context, MoviesActivity::class.java)
                context.startActivity(intent)
                (context as? Activity)?.finishAffinity()
            },
            text = { Text("Movies") }
        )
        DropdownMenuItem(
            onClick = {
                onDismissRequest()
                val intent = Intent(context, TvPageActivity::class.java)
                context.startActivity(intent)
                (context as? Activity)?.finishAffinity()
            },
            text = { Text("TV") }
        )
        DropdownMenuItem(
            onClick = {
                onDismissRequest()
                val intent = Intent(context, SearchPageActivity::class.java)
                context.startActivity(intent)
                (context as? Activity)?.finishAffinity()
            },
            text = { Text("Search") }
        )
        DropdownMenuItem(
            onClick = {
                onDismissRequest()
                val intent = Intent(context, ConfigurationActivity::class.java)
                context.startActivity(intent)
                (context as? Activity)?.finishAffinity()
            },
            text = { Text("Configuration") }
        )
        DropdownMenuItem(
            onClick = {
                onDismissRequest()
                // Direct Intent to LoginActivity (for logout)
                val intent = Intent(context, LoginActivity::class.java)
                context.startActivity(intent)
            },
            text = { Text("Logout") }
        )
    }
}
