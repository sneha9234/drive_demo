package com.example.drivedemo.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.api.services.drive.model.File

@Composable
fun FileItemCard(
    file: File,
    modifier: Modifier
) {
    Column(
        modifier = Modifier
            .fillMaxWidth().padding(5.dp)
    )
    {
        Row(
            modifier = modifier
                .fillMaxWidth().background(Color.Black),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
        )
        {
            Text(
                text = file.name,
                color = Color.White,
                modifier = Modifier.padding(vertical = 15.dp, horizontal = 10.dp)
            )
        }
    }
    Spacer(modifier = Modifier.height(2.dp))
}