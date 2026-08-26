package com.czczypsoson.datausagewidget

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle

class DataWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val usageManager = DataUsageManager(context)
        val bytesUsed = usageManager.getMonthlyMobileDataUsage()
        val formattedData = usageManager.formatDataSize(bytesUsed)

        provideContent {
            // Material 3 / Material You dynamické téma
            GlanceTheme {
                WidgetContent(dataText = formattedData)
            }
        }
    }
}

@Composable
private fun WidgetContent(dataText: String) {
    val context = LocalContext.current

    // Box představuje základní plochu (vrstvu), na kterou skládáme prvky
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.widgetBackground)
            .padding(8.dp)
    ) {
        // --- VRSTVA 1: TEXTY UPROSTŘED (Mírně posunuté nahoru) ---
        Column(
            modifier = GlanceModifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Malý odstup od horního okraje
            Spacer(modifier = GlanceModifier.height(12.dp))

            Text(
                text = "Used:",
                style = TextStyle(
                    color = GlanceTheme.colors.onSurfaceVariant,
                    fontSize = 12.sp
                )
            )

            // Přesný odstup mezi "Used:" a číslem
            Spacer(modifier = GlanceModifier.height(4.dp))

            Text(
                text = dataText,
                style = TextStyle(
                    color = GlanceTheme.colors.primary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }

        // --- VRSTVA 2: TLAČÍTKA NAHOŘE (Vykreslí se nad texty) ---
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // LEVÉ TLAČÍTKO: Nastavení
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = GlanceModifier
                        .size(36.dp)
                        .cornerRadius(18.dp)
                        .clickable(actionRunCallback<OpenSettingsAction>())
                ) {
                    Image(
                        provider = ImageProvider(R.drawable.ic_settings),
                        contentDescription = context.getString(R.string.widget_button_settings),
                        colorFilter = androidx.glance.ColorFilter.tint(GlanceTheme.colors.onSurface)
                    )
                }
            }

            Spacer(modifier = GlanceModifier.defaultWeight())

            // PRAVÉ TLAČÍTKO: Obnovení (Refresh)
            Box(
                contentAlignment = Alignment.Center,
                modifier = GlanceModifier
                    .size(
                    36.dp)
                    .cornerRadius(18.dp)
                    .clickable(actionRunCallback<RefreshAction>())
            ) {
                Image(
                    provider = ImageProvider(R.drawable.ic_refresh),
                    contentDescription = context.getString(R.string.widget_button_refresh),
                    colorFilter = androidx.glance.ColorFilter.tint(GlanceTheme.colors.onSurface)
                )
            }
        }
    }
}

class RefreshAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        // Vynutíme přepočet widgetu
        DataWidget().update(context, glanceId)
    }
}
class OpenSettingsAction : ActionCallback {
    @SuppressLint("InlinedApi")
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        // 1. Primární pokus: Otevřít přímo detailní nastavení spotřeby dat
        val primaryIntent = Intent(Settings.ACTION_DATA_USAGE_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        // 2. Záložní pokus: Otevřít obecné nastavení sítě a připojení
        val fallbackIntent = Intent(Settings.ACTION_WIRELESS_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        try {
            context.startActivity(primaryIntent)
        } catch (e: ActivityNotFoundException) {
            // Pokud telefon přímou obrazovku dat nemá/nepodporuje, otevře se záloha
            context.startActivity(fallbackIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

// Receiver pro registraci widgetu v systému
class DataWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = DataWidget()
}