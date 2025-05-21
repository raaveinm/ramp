package com.raaveinm.chirro.ui.fragments

import androidx.annotation.OptIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import com.raaveinm.chirro.R

@UnstableApi
@Composable
fun WelcomeScreen(
    modifier: Modifier = Modifier,
){
    var current: Int by remember { mutableIntStateOf(0) }

    val hello: String = stringResource(id = R.string.welcome_text)
    val start: String = stringResource(id = R.string.start_text)

    when(current){
        0 -> FirstScreen(modifier = modifier, hello = hello, start = start, onClick = { current++ })
    }
}

@Composable
fun FirstScreen(
    modifier: Modifier,
    hello: String,
    start: String,
    onClick: () -> Unit
){
    Column (
        modifier = modifier.padding(
            top = 80.dp,
            start = 10.dp,
            end = 10.dp,
            bottom = 10.dp
        ).fillMaxSize(),
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
    ){
        Text(
            text = hello,
            modifier = Modifier,
            style = MaterialTheme.typography.titleLarge,
            fontFamily = FontFamily.Monospace,
        )

        Spacer( modifier = Modifier.padding(40.dp) )

        Text(
            text = start,
            modifier = Modifier,
            style = MaterialTheme.typography.bodyLarge,
            fontFamily = FontFamily.Monospace,
            textAlign = TextAlign.Center
        )

        Image(
            painter = painterResource(id = R.drawable.chirro_1),
            contentDescription = "logo",
            modifier = Modifier.padding(top = 10.dp).scale(0.7F)
        )

        Button(
            onClick = { onClick() },
            modifier = Modifier
                .padding(50.dp).heightIn(60.dp).widthIn(200.dp)
                .shadow(8.dp, CircleShape, true, spotColor = Color(238,181,253,255)),
            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.onPrimary,
                contentColor = MaterialTheme.colorScheme.secondary
            )
        ) {
            Text(
                text = stringResource(id = R.string.welcome_text),
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = FontFamily.Monospace
            )

            Icon(
                modifier = Modifier
                    .padding(start = 10.dp)
                    .shadow(10.dp, CircleShape, true, spotColor = Color(238,181,253,255)),
                imageVector = (Icons.AutoMirrored.Filled.NavigateNext),
                contentDescription = "Next",

            )
        }
    }
}

@OptIn(UnstableApi::class)
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun WwelcomeScreen (){WelcomeScreen()}

@Preview (showBackground = true)
@Composable
fun FirstScreenPreview(){
    val hello: String = stringResource(id = R.string.welcome_text)
    val start: String = stringResource(id = R.string.start_button)
    FirstScreen(
        modifier = Modifier,
        hello = hello,
        start = start,
        onClick = {}
    )
}
