//Libraries
package com.app.android.presentation.view.atomicDesign.atomic

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

//We initiate our color variables
private val Blue = Color(0xFF2F6FB5)
private val DarkBlue = Color(0xFF12355B)
private val Beige = Color(0xFFF6F0E3)
private val GrayText = Color(0xFF6B655A)

//1. Button
//Function for the border of the button
//In this case is the dotted line, the first button
fun Modifier.dashedBorder(
    //We initialize every variable of color, length, width, etc
    color: Color,
    strokeWidth: Dp = 2.dp,
    cornerRadius: Dp = 24.dp,
    dashLength: Dp = 8.dp,
    gapLength: Dp = 6.dp
): Modifier = this.drawWithContent {
    drawContent()
    val stroke = strokeWidth.toPx()
    drawRoundRect(
        color = color,
        topLeft = Offset(stroke / 2, stroke / 2),
        size = Size(size.width - stroke, size.height - stroke),
        cornerRadius = CornerRadius(cornerRadius.toPx()),
        style = Stroke(
            width = stroke,
            pathEffect = PathEffect.dashPathEffect(
                floatArrayOf(dashLength.toPx(), gapLength.toPx()),
                0f
            )
        )
    )
}

//The second button inside the dotted button
@Composable
fun FileUploadCard(
    title: String,
    fileName: String,
    fileInfo: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .dashedBorder(color = Blue),
        shape = RoundedCornerShape(24.dp),
        color = Color.White
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            //Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.FileUpload,
                    contentDescription = null,
                    tint = Blue,
                    modifier = Modifier.size(28.dp)
                )
                Text(
                    text = title,
                    fontSize = 20.sp,
                    color = Color(0xFF3A3A3A)
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Beige)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Description,
                    contentDescription = null,
                    tint = Blue,
                    modifier = Modifier.size(28.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = fileName,
                        fontSize = 14.sp,
                        color = Color(0xFF3A3A3A)
                    )
                    Text(
                        text = fileInfo,
                        fontSize = 16.sp,
                        color = GrayText
                    )
                }
                Icon(
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = null,
                    tint = Blue,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FileUploadCardPreview() {
    //Data made up
    Box(modifier = Modifier.padding(16.dp)) {
        FileUploadCard(
            title = "Identificación oficial",
            //Variables to be changed
            fileName = "INE_Sofia_Garcia.pdf",
            fileInfo = "1.2 MB · Carga completa",
            //Function to call when you click de button
            onClick = {}
        )
    }
}

//Second button empty without data
@Composable
fun FileUploadCardEmpty(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(140.dp)
            .dashedBorder(color = Blue),
        shape = RoundedCornerShape(24.dp),
        color = Color.White
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(
                12.dp,
                Alignment.CenterHorizontally
            )
        ) {
            //Import icons needed in the card
            Icon(
                imageVector = Icons.Outlined.FileUpload,
                contentDescription = null,
                tint = Blue,
                modifier = Modifier.size(28.dp)
            )
            //Caracteristics of text
            Text(
                text = title,
                fontSize = 20.sp,
                color = Color(0xFF3A3A3A)
            )
        }
    }
}

//Function to xhow preview and call the onClick
@Preview(showBackground = true)
@Composable
fun FileUploadCardEmptyPreview() {
    Box(modifier = Modifier.padding(16.dp)) {
        FileUploadCardEmpty(
            title = "Identificación oficial",
            onClick = {}
        )
    }
}