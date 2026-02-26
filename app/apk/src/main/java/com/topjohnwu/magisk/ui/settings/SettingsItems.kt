package com.topjohnwu.magisk.ui.settings

import android.app.Activity
import android.content.res.Resources
import android.view.LayoutInflater
import android.widget.EditText
import com.topjohnwu.magisk.core.Config
import com.topjohnwu.magisk.core.Info
import com.topjohnwu.magisk.core.utils.MediaStoreUtils
import com.topjohnwu.magisk.view.MagiskDialog
import com.topjohnwu.magisk.core.R as CoreR

/**
 * 设置项工具对象
 * 提供设置项的辅助方法和对话框显示
 */

/**
 * 检查自定义更新通道是否启用
 */
object UpdateChannelUrl {
    fun isEnabled(): Boolean = Config.updateChannel == Config.Value.CUSTOM_CHANNEL

    fun getDescription(res: Resources): String? {
        return Config.customChannelUrl.takeIf { it.isNotEmpty() }
    }

    fun showDialog(activity: Activity, viewModel: SettingsViewModel) {
        val view = LayoutInflater.from(activity).inflate(
            com.topjohnwu.magisk.R.layout.dialog_settings_update_channel, null
        )
        val editText = view.findViewById<EditText>(
            com.topjohnwu.magisk.R.id.dialog_custom_download_text
        )
        editText.setText(Config.customChannelUrl)

        MagiskDialog(activity).apply {
            setTitle(CoreR.string.settings_update_custom)
            setView(view)
            setButton(MagiskDialog.ButtonType.POSITIVE) {
                text = android.R.string.ok
                onClick {
                    val newUrl = editText.text?.toString() ?: ""
                    Config.customChannelUrl = newUrl
                    Info.resetUpdate()
                    doNotDismiss = false
                }
            }
            setButton(MagiskDialog.ButtonType.NEGATIVE) {
                text = android.R.string.cancel
            }
        }.show()
    }
}

/**
 * 下载路径设置
 */
object DownloadPath {
    fun showDialog(activity: Activity, viewModel: SettingsViewModel) {
        val view = LayoutInflater.from(activity).inflate(
            com.topjohnwu.magisk.R.layout.dialog_settings_download_path, null
        )
        val titleText = view.findViewById<android.widget.TextView>(
            com.topjohnwu.magisk.R.id.dialog_custom_download_title
        )
        val editText = view.findViewById<EditText>(
            com.topjohnwu.magisk.R.id.dialog_custom_download_text
        )

        titleText.text = activity.getString(
            CoreR.string.settings_download_path_message,
            MediaStoreUtils.fullPath(Config.downloadDir)
        )
        editText.setText(Config.downloadDir)

        MagiskDialog(activity).apply {
            setTitle(CoreR.string.settings_download_path_title)
            setView(view)
            setButton(MagiskDialog.ButtonType.POSITIVE) {
                text = android.R.string.ok
                onClick {
                    val newPath = editText.text?.toString() ?: ""
                    Config.downloadDir = newPath
                    doNotDismiss = false
                }
            }
            setButton(MagiskDialog.ButtonType.NEGATIVE) {
                text = android.R.string.cancel
            }
        }.show()
    }
}

/**
 * 隐藏应用设置
 */
object Hide {
    fun showDialog(activity: Activity, viewModel: SettingsViewModel) {
        val view = LayoutInflater.from(activity).inflate(
            com.topjohnwu.magisk.R.layout.dialog_settings_app_name, null
        )
        val editText = view.findViewById<EditText>(
            com.topjohnwu.magisk.R.id.app_name
        )
        editText.setText("Settings")

        MagiskDialog(activity).apply {
            setTitle(CoreR.string.settings_hide_app_title)
            setMessage(CoreR.string.settings_hide_app_summary)
            setView(view)
            setButton(MagiskDialog.ButtonType.POSITIVE) {
                text = android.R.string.ok
                onClick {
                    val newName = editText.text?.toString() ?: "Settings"
                    if (newName.isNotBlank() && newName.length <= 30) {
                        viewModel.hideApp(newName)
                        doNotDismiss = false
                    } else {
                        doNotDismiss = true
                    }
                }
            }
            setButton(MagiskDialog.ButtonType.NEGATIVE) {
                text = android.R.string.cancel
            }
        }.show()
    }
}
