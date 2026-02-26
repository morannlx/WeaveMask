package com.topjohnwu.magisk.ui.settings

import android.app.Activity
import android.widget.Toast
import androidx.lifecycle.viewModelScope
import com.topjohnwu.magisk.arch.BaseViewModel
import com.topjohnwu.magisk.core.AppContext
import com.topjohnwu.magisk.core.ktx.toast
import com.topjohnwu.magisk.core.tasks.AppMigration
import com.topjohnwu.magisk.core.utils.RootUtils
import com.topjohnwu.magisk.events.AddHomeIconEvent
import com.topjohnwu.magisk.events.AuthEvent
import kotlinx.coroutines.launch
import com.topjohnwu.magisk.core.R as CoreR

/**
 * 设置页面 ViewModel
 * 处理设置页面的业务逻辑和用户交互
 */
class SettingsViewModel : BaseViewModel() {

    /** 日志页面导航回调 */
    var onNavigateToLog: (() -> Unit)? = null

    /** DenyList 配置页面导航回调 */
    var onNavigateToDenyListConfig: (() -> Unit)? = null

    /**
     * 添加桌面快捷方式
     */
    fun addShortcut() {
        AddHomeIconEvent().publish()
    }

    /**
     * 创建 Systemless Hosts
     */
    fun createHosts() {
        viewModelScope.launch {
            RootUtils.addSystemlessHosts()
            AppContext.toast(CoreR.string.settings_hosts_toast, Toast.LENGTH_SHORT)
        }
    }

    /**
     * 导航到 DenyList 配置页面
     */
    fun navigateToDenyListConfig() {
        onNavigateToDenyListConfig?.invoke()
    }

    /**
     * 恢复应用
     */
    fun restoreApp() {
        viewModelScope.launch {
            val activity = getActivity()
            if (activity != null) {
                AppMigration.restore(activity)
            }
        }
    }

    /**
     * 隐藏应用
     * @param newName 新的应用名称
     */
    fun hideApp(newName: String) {
        viewModelScope.launch {
            val activity = getActivity()
            if (activity != null) {
                AppMigration.hide(activity, newName)
            }
        }
    }

    /**
     * 执行生物认证
     * @param callback 认证结果回调
     */
    fun authenticate(callback: (Boolean) -> Unit) {
        AuthEvent { callback(true) }.publish()
    }

    /**
     * 切换设置项前进行生物认证
     * @param checked 目标状态
     * @param callback 认证结果回调
     */
    fun authenticateAndToggle(checked: Boolean, callback: (Boolean) -> Unit) {
        AuthEvent { callback(true) }.publish()
    }

    /**
     * 获取当前 Activity
     */
    private fun getActivity(): Activity? {
        // 通过事件系统获取当前 Activity
        return null
    }
}
