package com.olegkos.androidapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.olegkos.virtualnoveltesttwo.App
import com.olegkos.virtualnoveltesttwo.theme.VnAppTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            VnAppTheme {
                App()
            }
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    VnAppTheme {
        App()
    }
}