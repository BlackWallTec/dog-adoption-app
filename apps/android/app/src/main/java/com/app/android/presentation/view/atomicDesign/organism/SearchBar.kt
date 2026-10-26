package com.app.android.presentation.view.atomicDesign.organism

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp


@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String)->Unit,
    onFilterClick: ()-> Unit,
    modifier: Modifier=Modifier,

){
    Row(
        modifier=modifier.fillMaxWidth().background(Color(0xFFEAE3D4),RoundedCornerShape(50.dp)).padding(horizontal=16.dp),
        verticalAlignment=Alignment.CenterVertically
    ){
        Icon(
            imageVector=Icons.Default.Search,
            contentDescription="Buscar",
            tint=Color(0xFF2F6FB5)
        )
        TextField(
            value=query,
            onValueChange=onQueryChange,
            placeholder={Text("Buscar un compañero")},
            singleLine=true,
            modifier=Modifier.weight(1f),
            colors=TextFieldDefaults.colors(
                focusedContainerColor=Color.Transparent,
                unfocusedContainerColor=Color.Transparent,
                focusedIndicatorColor=Color.Transparent,
                unfocusedIndicatorColor=Color.Transparent
            )
        )
        IconButton( onClick=onFilterClick)
        {
            Icon(imageVector=Icons.Default.Tune, contentDescription="Filtros", tint=Color(0xFF2F6FB5)
            )
        }
    }
}
@Suppress("ktlint:standard:function-naming")
@Preview(showBackground=true, name="Search Bar")
@Composable
fun SearchBarPreview(){
    var query by remember { mutableStateOf("")}
    SearchBar(
        query=query,
        onQueryChange={ query=it },
        onFilterClick={}
    )
}