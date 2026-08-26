package com.czczypsoson.datausagewidget

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mikepenz.aboutlibraries.ui.compose.android.produceLibraries
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer
import androidx.compose.runtime.getValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val packageInfo = remember { getPackageInfo(context) }
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.about_title)) },
                navigationIcon = {
                    Tooltip(
                        {
                            IconButton(onClick = onBackClick) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                            }
                        },
                        stringResource(R.string.back)
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
        val libraries by produceLibraries(R.raw.aboutlibraries)

        LibrariesContainer(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            header = {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                    ) {
                        // Titulek aplikace
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold),
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Handmade text
                        Text(
                            text = stringResource(R.string.about_handmade),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD32F2F),
                            modifier = Modifier
                                .padding(top = 12.dp)
                                .fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 24.dp))

                        // Informační řádky využívající getString()
                        InfoRow(
                            stringResource(R.string.about_developed),
                            stringResource(R.string.about_developer)
                        )
                        InfoRow(stringResource(R.string.about_version), packageInfo.versionName)
                        InfoRow(
                            stringResource(R.string.about_version_code),
                            packageInfo.versionCode
                        )
                        InfoRow(stringResource(R.string.about_pkg_id), context.packageName)
                        InfoRow(stringResource(R.string.about_min_sdk), packageInfo.minSdk)
                        InfoRow(stringResource(R.string.about_target_sdk), packageInfo.targetSdk)

                        Spacer(modifier = Modifier.height(24.dp))

                        // Poděkování
                        Text(
                            text = stringResource(R.string.about_thanks),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(46.dp))
                    }
                }
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.about_libraries),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            libraries = libraries
        )
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
        Text(text = value, style = MaterialTheme.typography.bodyLarge)
    }
}

// Pomocná data a funkce
private data class AppPkgInfo(val versionName: String, val versionCode: String, val minSdk: String, val targetSdk: String)

private fun getPackageInfo(context: Context): AppPkgInfo {
    val pm = context.packageManager
    val info = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        pm.getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(0))
    } else {
        @Suppress("DEPRECATION") pm.getPackageInfo(context.packageName, 0)
    }
    val code = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) info.longVersionCode.toString() else @Suppress("DEPRECATION") info.versionCode.toString()

    return AppPkgInfo(
        versionName = info.versionName ?: "N/A",
        versionCode = code,
        minSdk = context.applicationInfo.minSdkVersion.toString(),
        targetSdk = context.applicationInfo.targetSdkVersion.toString()
    )
}