package com.tlc.feature.feature.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.tlc.feature.R

@Composable
fun NextBackButton(
    currentPage: Int,
    onNextClick: () -> Unit,
    onBackClick: () -> Unit,
    onGetStartedClick: () -> Unit,
){
    val context = LocalContext.current
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        if(currentPage != 0){
            TextButton(onClick = {
                onBackClick()
            }) {
                Text(text = "Back", style = TextStyle(
                    color = Color.Black
                )
                )
            }
        }
        Spacer(modifier = Modifier.size(8.dp))
        Button(onClick = {
            if(currentPage == 2){
                onGetStartedClick()
            }else{
                onNextClick()
            }
        },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Black,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(4.dp)
        ) {
            Text(text = if(currentPage == 2) context.getString(R.string.get_started) else context.getString(
                R.string.next))
        }

    }
}
