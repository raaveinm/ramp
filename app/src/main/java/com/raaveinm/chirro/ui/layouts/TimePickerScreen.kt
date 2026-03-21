package com.raaveinm.chirro.ui.layouts

import android.annotation.SuppressLint
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TimePickerScreen(
    hourState: LazyListState = rememberLazyListState(initialFirstVisibleItemIndex = 0),
    minuteState: LazyListState = rememberLazyListState(initialFirstVisibleItemIndex = 0),
    secondState: LazyListState = rememberLazyListState(initialFirstVisibleItemIndex = 0)
) {

    val hours = (0..23).toList()
    val minutesAndSeconds = (0..59).toList()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            WheelPicker(items = hours, state = hourState)
            TimeSeparator()
            WheelPicker(items = minutesAndSeconds, state = minuteState)
            TimeSeparator()
            WheelPicker(items = minutesAndSeconds, state = secondState)
        }
    }
}

@Composable
fun TimeSeparator() {
    Text(
        text = ":",
        fontSize = 14.sp,
        fontWeight = FontWeight.Light,
        color = Color.White,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@SuppressLint("DefaultLocale")
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WheelPicker(
    items: List<Int>,
    state: LazyListState,
    modifier: Modifier = Modifier
) {
    val itemHeight = 48.dp
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = state)
    val selectedIndex by remember { derivedStateOf { state.firstVisibleItemIndex } }

    LazyColumn(
        state = state,
        flingBehavior = flingBehavior,
        modifier = modifier
            .height(itemHeight * 3)
            .width(64.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item { Spacer(modifier = Modifier.height(itemHeight)) }
        items(items.size) { index ->
            val isSelected = index == selectedIndex
            val value = items[index]

            Box(
                modifier = Modifier.height(itemHeight).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = String.format("%02d", value),
                    fontSize = if (isSelected) 24.sp else 18.sp,
                    fontWeight = if (isSelected) FontWeight.Normal else FontWeight.ExtraLight,
                    color = if (isSelected) Color.White else Color(0xFF333333)
                )
            }
        }

        item { Spacer(modifier = Modifier.height(itemHeight)) }
    }
}

@Preview
@Composable
fun TimePickerScreenPreview() {
    TimePickerScreen()
}