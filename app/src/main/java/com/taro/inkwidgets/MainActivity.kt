package com.taro.inkwidgets

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.taro.inkwidgets.ui.InkApp
import com.taro.inkwidgets.ui.theme.InkWidgetsTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Edge-to-edge with light (Paper) system bars and dark icons.
        val paper = getColor(R.color.paper)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(paper, paper),
            navigationBarStyle = SystemBarStyle.light(paper, paper),
        )
        super.onCreate(savedInstanceState)

        // Opening the app re-arms the minute alarm in case MIUI killed it.
        ClockWidgetProvider.updateAll(this)
        SearchWidgetProvider.updateAll(this)

        setContent {
            InkWidgetsTheme {
                InkApp()
            }
        }
    }
}
