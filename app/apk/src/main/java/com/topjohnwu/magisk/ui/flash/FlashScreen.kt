package com.topjohnwu.magisk.ui.flash

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Save
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Observer
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.topjohnwu.magisk.core.R as CoreR
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CircularProgressIndicator
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun FlashScreen(
    viewModel: FlashViewModel,
    action: String,
    additionalData: Uri?,
    onNavigateBack: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var state by remember { mutableStateOf(viewModel.state.value ?: FlashViewModel.State.FLASHING) }
    val lines by viewModel.consoleLines.collectAsState()
    val isFlashing = state == FlashViewModel.State.FLASHING

    DisposableEffect(viewModel, lifecycleOwner) {
        val observer = Observer<FlashViewModel.State> {
            state = it
        }
        viewModel.state.observe(lifecycleOwner, observer)
        onDispose {
            viewModel.state.removeObserver(observer)
        }
    }

    BackHandler(enabled = isFlashing) {}

    LaunchedEffect(action, additionalData) {
        viewModel.prepareForCompose(action = action, uri = additionalData)
        viewModel.startFlashing()
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = context.getString(CoreR.string.flash_screen_title),
                navigationIcon = {
                    IconButton(
                        modifier = Modifier.padding(start = 16.dp),
                        onClick = {
                            if (!isFlashing) onNavigateBack()
                        }
                    ) {
                        Icon(
                            imageVector = MiuixIcons.Back,
                            contentDescription = null,
                            tint = MiuixTheme.colorScheme.onBackground
                        )
                    }
                },
                actions = {
                    IconButton(
                        modifier = Modifier.padding(end = 16.dp),
                        onClick = { viewModel.saveLogForCompose(context) }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Save,
                            contentDescription = null,
                            tint = MiuixTheme.colorScheme.onBackground
                        )
                    }
                }
            )
        },
        contentWindowInsets = WindowInsets.systemBars.add(WindowInsets.displayCutout).only(WindowInsetsSides.Horizontal),
        content = { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    Spacer(modifier = Modifier.height(8.dp))
                    FlashStateCard(state = state)
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                    ) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            itemsIndexed(lines) { _, line ->
                                Text(
                                    text = line,
                                    style = MiuixTheme.textStyles.body2.copy(
                                        fontFamily = FontFamily.Monospace,
                                        lineHeight = 18.sp
                                    )
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                if (state == FlashViewModel.State.SUCCESS && viewModel.showReboot) {
                    Button(
                        onClick = { viewModel.restartPressed() },
                        colors = ButtonDefaults.buttonColorsPrimary(),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 16.dp, bottom = 16.dp)
                    ) {
                        Text(
                            text = context.getString(CoreR.string.reboot),
                            color = MiuixTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    )
}

@Composable
private fun FlashStateCard(state: FlashViewModel.State) {
    val context = LocalContext.current
    val text = when (state) {
        FlashViewModel.State.FLASHING -> context.getString(CoreR.string.flashing)
        FlashViewModel.State.SUCCESS -> context.getString(CoreR.string.done)
        FlashViewModel.State.FAILED -> context.getString(CoreR.string.failure)
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (state == FlashViewModel.State.FLASHING) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(10.dp))
            }
            Text(
                text = text,
                style = MiuixTheme.textStyles.body1,
                fontWeight = FontWeight.Bold,
                color = when (state) {
                    FlashViewModel.State.SUCCESS -> MiuixTheme.colorScheme.primary
                    FlashViewModel.State.FAILED -> MiuixTheme.colorScheme.error
                    FlashViewModel.State.FLASHING -> Color.Unspecified
                }
            )
        }
    }
}
