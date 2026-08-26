package com.czczypsoson.datausagewidget.ui.theme

import androidx.compose.ui.graphics.Color

object Colors {
    val White = Color(0xFFFFFFFF)
    val Black = Color(0xFF000000)

    object DeviceCardBorder {
        val Connected = Color(0xFF5d84de)
        val Disconnected = Color(0xFFd91e1e)
    }

    object DeviceCardPlaying {
        val Stopped = Color(0xFF919191)
        val Playing = Color(0xFF399c16)
        val StoppedLoop = Color(0xFF91894d)
        val PlayingLoop = Color(0xFFd9ff30)
    }
}