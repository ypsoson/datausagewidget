package com.czczypsoson.datausagewidget

import com.czczypsoson.datausagewidget.BuildConfig
import android.app.AppOpsManager
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.czczypsoson.datausagewidget.ui.DataUsageWidgetTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.URL


sealed class Screen(val route: String) {
    object Main : Screen("main_screen")
    object About : Screen("about_screen")
}

class MainActivity : ComponentActivity() {
    private val hasPermissionState = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }

        setContent {
            val context = LocalContext.current

            DataUsageWidgetTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    NavHost(
                        navController = navController,
                        startDestination = Screen.Main.route,
                        // 1. Animace, když obrazovka PŘICHÁZÍ na scénu
                        enterTransition = {
                            slideIntoContainer(
                                towards = AnimatedContentTransitionScope.SlideDirection.Up,
                                animationSpec = tween(400)
                            ) + fadeIn(animationSpec = tween(400))
                        },
                        // 2. Animace, když obrazovka ODCHÁZÍ (stará obrazovka mizí, když jdeš na novou)
                        exitTransition = {
                            slideOutOfContainer(
                                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                                animationSpec = tween(400)
                            ) + fadeOut(animationSpec = tween(400))
                        },
                        // 3. Animace, když se VRACÍŠ zpět - opačný směr
                        popEnterTransition = {
                            slideIntoContainer(
                                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                                animationSpec = tween(400)
                            ) + fadeIn(animationSpec = tween(400))
                        },
                        // 4. Animace, když obrazovka mizí při návratu zpět
                        popExitTransition = {
                            slideOutOfContainer(
                                towards = AnimatedContentTransitionScope.SlideDirection.Down,
                                animationSpec = tween(400)
                            ) + fadeOut(animationSpec = tween(400))
                        }
                    ) {
                        composable(Screen.Main.route) {
                            MainScreen(
                                context = context,
                                hasPermission = hasPermissionState.value,
                                onRequestPermission = {
                                    // Vytvoříme intent přímo pro systémové nastavení prístupu k datům
                                    val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                                        // Přidáme odkaz přímo na balíček naší aplikace
                                        data = Uri.fromParts("package", packageName, null)
                                    }

                                    try {
                                        startActivity(intent)
                                    } catch (e: Exception) {
                                        // Pokud by telefon přímo zobrazení tvé aplikace nepodporoval,
                                        // otevře se jako záloha obecný seznam aplikací
                                        startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                                    }
                                },
                                onAboutScreen = {
                                    navController.navigate(Screen.About.route)
                                }
                            )
                        }
                        composable(Screen.About.route) {
                            AboutScreen(
                                onBackClick = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        hasPermissionState.value = hasUsageStatsPermission(this)
    }

    private fun hasUsageStatsPermission(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                context.packageName
            )
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                context.packageName
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    context: Context,
    hasPermission: Boolean,
    onRequestPermission: () -> Unit,
    onAboutScreen: () -> Unit
) {
    val uriHandler = LocalUriHandler.current
    var updateRelease by remember { mutableStateOf<GithubRelease?>(null) }

    // Spustí se automaticky jednou při startu aplikace
    LaunchedEffect(Unit) {
        val release = checkForUpdate()
        if (release != null) {
            val currentVersion = BuildConfig.VERSION_NAME
            if (isNewVersionAvailable(currentVersion, release.tag_name)) {
                updateRelease = release
            }
        }
    }

    // Pokud je k dispozici nová verze, ukáže se dialog
    updateRelease?.let { release ->
        UpdateDialogWithBrowser(
            newVersion = release.tag_name,
            releaseUrl = release.html_url,
            onDismiss = { updateRelease = null }
        )
    }


    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    Tooltip(
                        {
                            IconButton(
                                onClick = onAboutScreen
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Info,
                                    contentDescription = stringResource(R.string.top_about)
                                )
                            }
                        },
                        stringResource(R.string.top_about)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (hasPermission) {
                Text(
                    text = stringResource(R.string.permissions_granted),
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = { requestAddWidget(context) }) {
                    Text(stringResource(R.string.widget_pin))
                }
            } else {
                Text(
                    text = stringResource(R.string.permissions_needed),
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onRequestPermission) {
                    Text(stringResource(R.string.permissions_settings))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Tooltip(vnitrek: @Composable () -> Unit, text: String, podminka: Boolean = true) {
    if (podminka) {
        TooltipBox(
            positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
            tooltip = {
                PlainTooltip() {
                    Text(text)
                }
            },
            state = rememberTooltipState()
        ) { vnitrek() }
    } else {
        vnitrek()
    }
}

private fun requestAddWidget(context: Context) {
    val appWidgetManager = context.getSystemService(AppWidgetManager::class.java)
    val myProvider = ComponentName(context, DataWidgetReceiver::class.java)

    if (appWidgetManager.isRequestPinAppWidgetSupported) {
        // Vyvolá systémový dialog pro přidání widgetu na plochu
        appWidgetManager.requestPinAppWidget(myProvider, null, null)
    }
}


@Composable
fun UpdateDialogWithBrowser(
    newVersion: String,
    releaseUrl: String,
    onDismiss: () -> Unit
) {
    val uriHandler = LocalUriHandler.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.update_title)) },
        text = {
            Column() {
                Text(stringResource(R.string.update_text))
                Text(stringResource(R.string.update_current) + ": " + BuildConfig.VERSION_NAME)
                Text(stringResource(R.string.update_new) + ": " + newVersion)
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    uriHandler.openUri(releaseUrl)
                    onDismiss()
                },
                colors = ButtonDefaults.textButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Text(stringResource(R.string.update_download))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Text(stringResource(R.string.update_ignore))
            }
        }
    )
}

@Serializable
data class GithubRelease(
    val tag_name: String,
    val html_url: String
)

suspend fun checkForUpdate(): GithubRelease? = withContext(Dispatchers.IO) {
    try {
        val url = URL("https://api.github.com/repos/ypsoson/datausagewidget/releases/latest")
        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            setRequestProperty("User-Agent", "Android-App")
            connectTimeout = 5000
            readTimeout = 5000
        }

        if (connection.responseCode == 200) {
            val responseJson = connection.inputStream.bufferedReader().use { it.readText() }
            val json = Json { ignoreUnknownKeys = true }
            return@withContext json.decodeFromString<GithubRelease>(responseJson)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return@withContext null
}
fun isNewVersionAvailable(current: String, latest: String): Boolean {
    val cleanCurrent = current.removePrefix("v")
    val cleanLatest = latest.removePrefix("v")
    return cleanCurrent != cleanLatest && cleanLatest > cleanCurrent
}