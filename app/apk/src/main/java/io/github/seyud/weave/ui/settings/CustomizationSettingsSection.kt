package io.github.seyud.weave.ui.settings

import android.app.Activity
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.AddToHomeScreen
import androidx.compose.material.icons.rounded.Adb
import androidx.compose.material.icons.rounded.AspectRatio
import androidx.compose.material.icons.rounded.BlurOn
import androidx.compose.material.icons.rounded.CallToAction
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.RoundedCorner
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.ui.unit.dp
import io.github.seyud.weave.core.App as CoreApp
import io.github.seyud.weave.core.Config
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Slider
import top.yukonga.miuix.kmp.basic.SliderDefaults
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.preference.OverlayDropdownPreference
import top.yukonga.miuix.kmp.preference.SwitchPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme
import io.github.seyud.weave.core.R as CoreR

@Composable
internal fun CustomizationSettingsSection(
    visibility: SettingsVisibility,
    onNavigateToAppLanguage: () -> Unit,
    onAddShortcut: () -> Unit,
) {
    val context = LocalContext.current
    val activity = context as Activity
    val res = context.resources
    val showScaleDialog = rememberSaveable { mutableStateOf(false) }
    var enableBlur by rememberSaveable { mutableStateOf(Config.enableBlur) }
    var enableFloatingBottomBar by rememberSaveable { mutableStateOf(Config.enableFloatingBottomBar) }
    var enableFloatingBottomBarBlur by rememberSaveable { mutableStateOf(Config.enableFloatingBottomBarBlur) }
    var enableSmoothCorner by rememberSaveable { mutableStateOf(Config.enableSmoothCorner) }
    var homeLayoutMode by rememberSaveable { mutableIntStateOf(Config.homeLayoutMode) }
    var themeMode by rememberSaveable { mutableIntStateOf(Config.colorMode) }
    var sliderValue by rememberSaveable { mutableStateOf(Config.pageScale) }

    SmallTitle(text = stringResource(CoreR.string.settings_customization))
    Card(modifier = Modifier.fillMaxWidth()) {
        val themeItems = listOf(
            stringResource(CoreR.string.settings_theme_mode_system),
            stringResource(CoreR.string.settings_theme_mode_light),
            stringResource(CoreR.string.settings_theme_mode_dark),
            stringResource(CoreR.string.settings_theme_mode_monet_system),
            stringResource(CoreR.string.settings_theme_mode_monet_light),
            stringResource(CoreR.string.settings_theme_mode_monet_dark),
        )
        OverlayDropdownPreference(
            title = stringResource(CoreR.string.settings_theme),
            summary = stringResource(CoreR.string.settings_theme_summary),
            items = themeItems,
            startAction = {
                Icon(
                    Icons.Rounded.Palette,
                    modifier = Modifier.padding(end = 6.dp),
                    contentDescription = null,
                    tint = colorScheme.onBackground,
                )
            },
            selectedIndex = themeMode,
            onSelectedIndexChange = { index ->
                Config.colorMode = index
                themeMode = index
            },
        )

        AnimatedVisibility(visible = themeMode in 3..5) {
            val colorItems = listOf(
                stringResource(CoreR.string.settings_key_color_default),
                stringResource(CoreR.string.color_red),
                stringResource(CoreR.string.color_pink),
                stringResource(CoreR.string.color_purple),
                stringResource(CoreR.string.color_deep_purple),
                stringResource(CoreR.string.color_indigo),
                stringResource(CoreR.string.color_blue),
                stringResource(CoreR.string.color_cyan),
                stringResource(CoreR.string.color_teal),
                stringResource(CoreR.string.color_green),
                stringResource(CoreR.string.color_yellow),
                stringResource(CoreR.string.color_amber),
                stringResource(CoreR.string.color_orange),
                stringResource(CoreR.string.color_brown),
                stringResource(CoreR.string.color_blue_grey),
                stringResource(CoreR.string.color_sakura),
            )
            val colorValues = listOf(
                0,
                Color(0xFFF44336).toArgb(),
                Color(0xFFE91E63).toArgb(),
                Color(0xFF9C27B0).toArgb(),
                Color(0xFF673AB7).toArgb(),
                Color(0xFF3F51B5).toArgb(),
                Color(0xFF2196F3).toArgb(),
                Color(0xFF00BCD4).toArgb(),
                Color(0xFF009688).toArgb(),
                Color(0xFF4FAF50).toArgb(),
                Color(0xFFFFEB3B).toArgb(),
                Color(0xFFFFC107).toArgb(),
                Color(0xFFFF9800).toArgb(),
                Color(0xFF795548).toArgb(),
                Color(0xFF607D8F).toArgb(),
                Color(0xFFFF9CA8).toArgb(),
            )
            var keyColorIndex by rememberSaveable {
                mutableIntStateOf(
                    colorValues.indexOf(Config.keyColor).takeIf { it >= 0 } ?: 0,
                )
            }

            OverlayDropdownPreference(
                title = stringResource(CoreR.string.settings_key_color),
                summary = stringResource(CoreR.string.settings_key_color_summary),
                items = colorItems,
                startAction = {
                    Icon(
                        Icons.Rounded.Palette,
                        modifier = Modifier.padding(end = 6.dp),
                        contentDescription = null,
                        tint = colorScheme.onBackground,
                    )
                },
                selectedIndex = keyColorIndex,
                onSelectedIndexChange = { index ->
                    Config.keyColor = colorValues[index]
                    keyColorIndex = index
                },
            )
        }

        val homeLayoutItems = listOf(
            stringResource(CoreR.string.settings_home_layout_classic),
            stringResource(CoreR.string.settings_home_layout_weavsk),
        )
        OverlayDropdownPreference(
            title = stringResource(CoreR.string.settings_home_layout),
            summary = stringResource(CoreR.string.settings_home_layout_summary),
            items = homeLayoutItems,
            startAction = {
                Icon(
                    Icons.Rounded.Home,
                    modifier = Modifier.padding(end = 6.dp),
                    contentDescription = null,
                    tint = colorScheme.onBackground,
                )
            },
            selectedIndex = homeLayoutMode.coerceIn(0, homeLayoutItems.lastIndex),
            onSelectedIndexChange = { index ->
                Config.homeLayoutMode = index
                homeLayoutMode = Config.homeLayoutMode
            },
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            SwitchPreference(
                title = stringResource(CoreR.string.settings_enable_blur),
                summary = stringResource(CoreR.string.settings_enable_blur_summary),
                startAction = {
                    Icon(
                        Icons.Rounded.WaterDrop,
                        modifier = Modifier.padding(end = 6.dp),
                        contentDescription = stringResource(CoreR.string.settings_enable_blur),
                        tint = colorScheme.onBackground,
                    )
                },
                checked = enableBlur,
                onCheckedChange = {
                    Config.enableBlur = it
                    enableBlur = Config.enableBlur
                    enableFloatingBottomBarBlur = Config.enableFloatingBottomBarBlur
                },
            )
        }

        SwitchPreference(
            title = stringResource(CoreR.string.settings_floating_bottom_bar),
            summary = stringResource(CoreR.string.settings_floating_bottom_bar_summary),
            startAction = {
                Icon(
                    Icons.Rounded.CallToAction,
                    modifier = Modifier.padding(end = 6.dp),
                    contentDescription = stringResource(CoreR.string.settings_floating_bottom_bar),
                    tint = colorScheme.onBackground,
                )
            },
            checked = enableFloatingBottomBar,
            onCheckedChange = {
                Config.enableFloatingBottomBar = it
                enableFloatingBottomBar = it
            },
        )

        AnimatedVisibility(
            visible = enableFloatingBottomBar && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU,
        ) {
            SwitchPreference(
                title = stringResource(CoreR.string.settings_enable_glass),
                summary = stringResource(CoreR.string.settings_enable_glass_summary),
                startAction = {
                    Icon(
                        Icons.Rounded.BlurOn,
                        modifier = Modifier.padding(end = 6.dp),
                        contentDescription = stringResource(CoreR.string.settings_enable_glass),
                        tint = colorScheme.onBackground,
                    )
                },
                checked = enableFloatingBottomBarBlur,
                onCheckedChange = {
                    Config.enableFloatingBottomBarBlur = it
                    enableFloatingBottomBarBlur = Config.enableFloatingBottomBarBlur
                    enableBlur = Config.enableBlur
                },
            )
        }

        SwitchPreference(
            title = stringResource(CoreR.string.settings_smooth_corner),
            summary = stringResource(CoreR.string.settings_smooth_corner_summary),
            startAction = {
                Icon(
                    Icons.Rounded.RoundedCorner,
                    modifier = Modifier.padding(end = 6.dp),
                    contentDescription = stringResource(CoreR.string.settings_smooth_corner),
                    tint = colorScheme.onBackground,
                )
            },
            checked = enableSmoothCorner,
            onCheckedChange = {
                Config.enableSmoothCorner = it
                enableSmoothCorner = it
            },
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            var enablePredictiveBack by rememberSaveable { mutableStateOf(Config.enablePredictiveBack) }
            SwitchPreference(
                title = stringResource(CoreR.string.settings_enable_predictive_back),
                summary = stringResource(CoreR.string.settings_enable_predictive_back_summary),
                startAction = {
                    Icon(
                        Icons.Rounded.Adb,
                        modifier = Modifier.padding(end = 6.dp),
                        contentDescription = stringResource(CoreR.string.settings_enable_predictive_back),
                        tint = colorScheme.onBackground,
                    )
                },
                checked = enablePredictiveBack,
                onCheckedChange = {
                    Config.enablePredictiveBack = it
                    enablePredictiveBack = it
                    CoreApp.setEnableOnBackInvokedCallback(context.applicationInfo, it)
                    activity.recreate()
                },
            )
        }

        ArrowPreference(
            title = stringResource(CoreR.string.settings_page_scale),
            summary = stringResource(CoreR.string.settings_page_scale_summary),
            startAction = {
                Icon(
                    Icons.Rounded.AspectRatio,
                    modifier = Modifier.padding(end = 6.dp),
                    contentDescription = stringResource(CoreR.string.settings_page_scale),
                    tint = colorScheme.onBackground,
                )
            },
            endActions = {
                Text(
                    text = "${(sliderValue * 100).toInt()}%",
                    color = colorScheme.onSurfaceVariantActions,
                )
            },
            onClick = { showScaleDialog.value = !showScaleDialog.value },
            holdDownState = showScaleDialog.value,
            bottomAction = {
                Slider(
                    value = sliderValue,
                    onValueChange = { sliderValue = it },
                    onValueChangeFinished = {
                        Config.pageScale = sliderValue
                    },
                    valueRange = 0.8f..1.1f,
                    showKeyPoints = true,
                    keyPoints = listOf(0.8f, 0.9f, 1f, 1.1f),
                    magnetThreshold = 0.01f,
                    hapticEffect = SliderDefaults.SliderHapticEffect.Step,
                )
            },
        )
        ScaleDialog(
            showDialog = showScaleDialog,
            scaleState = { Config.pageScale },
            onScaleChange = {
                Config.pageScale = it
                sliderValue = it
            },
        )

        ArrowPreference(
            title = stringResource(CoreR.string.language),
            summary = appLanguageSummary(res),
            startAction = {
                Icon(
                    Icons.Rounded.Language,
                    modifier = Modifier.padding(end = 6.dp),
                    contentDescription = null,
                    tint = colorScheme.onBackground,
                )
            },
            onClick = onNavigateToAppLanguage,
        )

        if (visibility.showAddShortcut) {
            ArrowPreference(
                title = stringResource(CoreR.string.add_shortcut_title),
                summary = stringResource(CoreR.string.setting_add_shortcut_summary),
                startAction = {
                    Icon(
                        Icons.AutoMirrored.Rounded.AddToHomeScreen,
                        modifier = Modifier.padding(end = 6.dp),
                        contentDescription = null,
                        tint = colorScheme.onBackground,
                    )
                },
                onClick = onAddShortcut,
            )
        }
    }
}
