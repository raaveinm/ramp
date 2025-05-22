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
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
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
    var current: Int by rememberSaveable { mutableIntStateOf(0) }

    val hello: String = stringResource(id = R.string.welcome_text)
    val start: String = stringResource(id = R.string.start_text)

    when(current){
        0 -> FirstScreen(modifier = modifier, hello = hello, start = start, onClick = { current++ })
        1 -> SecondScreen(modifier = modifier, hello = hello, onClick = { current++ })
        2 -> null
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
                .shadow(8.dp, CircleShape, true,
                    spotColor = Color(238,181,253,255)),
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
                    .shadow(10.dp, CircleShape, true,
                        spotColor = Color(238,181,253,255)),
                imageVector = (Icons.AutoMirrored.Filled.NavigateNext),
                contentDescription = "Next",

            )
        }
    }
}

@Composable
fun SecondScreen(
    modifier: Modifier,
    hello: String,
    onClick: () -> Unit
){
    val text: String = stringResource(R.string.introduction_text)
    val continueButton: String = stringResource(R.string.continue_button)
    val url = "https://github.com/raaveinm/ramp"
    val uriHandler = LocalUriHandler.current

    Column (
        modifier = modifier.padding(
            top = 80.dp,
            start = 10.dp,
            end = 10.dp,
            bottom = 10.dp
        ).fillMaxSize(),
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
    ) {
        Text(
            text = hello,
            modifier = Modifier,
            style = MaterialTheme.typography.titleLarge,
            fontFamily = FontFamily.Monospace,
        )

        Text(
            text = text,
            modifier = Modifier.padding(top = 60.dp, start = 20.dp, end = 20.dp),
            style = MaterialTheme.typography.bodyLarge,
            fontFamily = FontFamily.Monospace,
            textAlign = TextAlign.Center
        )

        Button(
            onClick = { uriHandler.openUri(url) },
            modifier = Modifier
                .padding(top = 150.dp).heightIn(60.dp).widthIn(200.dp)
                .shadow(8.dp, CircleShape, true,
                    spotColor = Color(238,181,253,255)),
            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.onPrimary,
                contentColor = MaterialTheme.colorScheme.secondary
            )
        ) {
            Text(
                text = ("github"),
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = FontFamily.Monospace
            )
            Icon(
                modifier = Modifier
                    .padding(start = 20.dp)
                    .shadow(10.dp, CircleShape, true,
                        spotColor = Color(238,181,253,255)),
                imageVector = (Icons.AutoMirrored.Filled.Login),
                contentDescription = "Next",
            )
        }

        Button(
            onClick = { onClick() },
            modifier = Modifier
                .padding(top = 30.dp).heightIn(60.dp).widthIn(200.dp)
                .shadow(8.dp, CircleShape, true,
                    spotColor = Color(238,181,253,255)),
            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.onPrimary,
                contentColor = MaterialTheme.colorScheme.secondary
            )
        ) {
            Text(
                text = (continueButton),
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = FontFamily.Monospace
            )
            Icon(
                modifier = Modifier
                    .padding(start = 10.dp)
                    .shadow(10.dp, CircleShape, true,
                        spotColor = Color(238,181,253,255)),
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

@Preview (showBackground = true,)
@Composable
fun SecondScreenPreview(){
    val hello: String = stringResource(id = R.string.welcome_text)
    SecondScreen(
        modifier = Modifier.fillMaxSize(),
        hello = hello,
        onClick = {}
    )
}
