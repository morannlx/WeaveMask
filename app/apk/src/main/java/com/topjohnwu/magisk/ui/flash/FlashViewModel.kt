package com.topjohnwu.magisk.ui.flash

import android.content.Context
import android.view.MenuItem
import android.widget.Toast
import androidx.databinding.Bindable
import androidx.databinding.ObservableArrayList
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.topjohnwu.magisk.BR
import com.topjohnwu.magisk.R
import com.topjohnwu.magisk.arch.BaseViewModel
import com.topjohnwu.magisk.core.Const
import com.topjohnwu.magisk.core.Info
import com.topjohnwu.magisk.core.ktx.reboot
import com.topjohnwu.magisk.core.ktx.synchronized
import com.topjohnwu.magisk.core.ktx.timeFormatStandard
import com.topjohnwu.magisk.core.ktx.toast
import com.topjohnwu.magisk.core.ktx.toTime
import com.topjohnwu.magisk.core.tasks.FlashZip
import com.topjohnwu.magisk.core.tasks.MagiskInstaller
import com.topjohnwu.magisk.core.utils.MediaStoreUtils
import com.topjohnwu.magisk.core.utils.MediaStoreUtils.outputStream
import com.topjohnwu.magisk.databinding.set
import com.topjohnwu.magisk.events.SnackbarEvent
import com.topjohnwu.superuser.CallbackList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FlashViewModel : BaseViewModel() {

    enum class State {
        FLASHING, SUCCESS, FAILED
    }

    private val _state = MutableLiveData(State.FLASHING)
    val state: LiveData<State> get() = _state
    val flashing = state.map { it == State.FLASHING }
    private var isInitialized = false
    private var isFlashingStarted = false

    @get:Bindable
    var showReboot = Info.isRooted
        set(value) = set(value, field, { field = it }, BR.showReboot)

    val items = ObservableArrayList<ConsoleItem>()
    lateinit var args: FlashFragmentArgs
    private val _consoleLines = MutableStateFlow<List<String>>(emptyList())
    val consoleLines: StateFlow<List<String>> = _consoleLines.asStateFlow()

    private val console: MutableList<String>
        get() = logItems

    private val logItems = mutableListOf<String>().synchronized()
    private val outItems = object : CallbackList<String>() {
        override fun onAddElement(e: String?) {
            e ?: return
            items.add(ConsoleItem(e))
            logItems.add(e)
            viewModelScope.launch {
                _consoleLines.value = _consoleLines.value + e
            }
        }
    }

    /**
     * 准备 Compose 界面的刷写环境
     * 每次进入 Flash 页面时都会调用，重置所有状态以确保可以重复执行安装
     *
     * @param action 刷写操作类型（如 FLASH_MAGISK、PATCH_FILE 等）
     * @param uri 附加数据 URI（如修补文件时的文件 URI）
     */
    fun prepareForCompose(action: String, uri: android.net.Uri?) {
        // 重置状态标志，允许重复初始化
        isInitialized = true
        isFlashingStarted = false

        // 重新初始化参数和状态
        args = FlashFragmentArgs(action = action, additionalData = uri)
        _state.value = State.FLASHING
        showReboot = Info.isRooted
        items.clear()
        synchronized(logItems) {
            logItems.clear()
        }
        _consoleLines.value = emptyList()
    }

    fun startFlashing() {
        if (isFlashingStarted) return
        isFlashingStarted = true
        val (action, uri) = args

        viewModelScope.launch {
            try {
                val result = when (action) {
                    Const.Value.FLASH_ZIP -> {
                        if (uri == null) {
                            console.add("Error: No file selected")
                            false
                        } else {
                            FlashZip(uri, outItems, logItems).exec()
                        }
                    }
                    Const.Value.UNINSTALL -> {
                        showReboot = false
                        MagiskInstaller.Uninstall(outItems, logItems).exec()
                    }
                    Const.Value.FLASH_MAGISK -> {
                        if (Info.isEmulator)
                            MagiskInstaller.Emulator(outItems, logItems).exec()
                        else
                            MagiskInstaller.Direct(outItems, logItems).exec()
                    }
                    Const.Value.FLASH_INACTIVE_SLOT -> {
                        showReboot = false
                        MagiskInstaller.SecondSlot(outItems, logItems).exec()
                    }
                    Const.Value.PATCH_FILE -> {
                        if (uri == null) {
                            console.add("Error: No file selected")
                            false
                        } else {
                            showReboot = false
                            MagiskInstaller.Patch(uri, outItems, logItems).exec()
                        }
                    }
                    else -> {
                        console.add("Error: Unknown action: $action")
                        false
                    }
                }
                onResult(result)
            } catch (e: Exception) {
                console.add("Error: ${e.message}")
                e.printStackTrace()
                onResult(false)
            }
        }
    }

    private fun onResult(success: Boolean) {
        _state.value = if (success) State.SUCCESS else State.FAILED
    }

    fun onMenuItemClicked(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_save -> savePressed()
        }
        return true
    }

    private fun savePressed() = withExternalRW {
        viewModelScope.launch(Dispatchers.IO) {
            val name = "magisk_install_log_%s.log".format(
                System.currentTimeMillis().toTime(timeFormatStandard)
            )
            val file = MediaStoreUtils.getFile(name)
            file.uri.outputStream().bufferedWriter().use { writer ->
                synchronized(logItems) {
                    logItems.forEach {
                        writer.write(it)
                        writer.newLine()
                    }
                }
            }
            SnackbarEvent(file.toString()).publish()
        }
    }

    fun saveLogForCompose(context: Context) {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    val name = "magisk_install_log_%s.log".format(
                        System.currentTimeMillis().toTime(timeFormatStandard)
                    )
                    val file = MediaStoreUtils.getFile(name)
                    file.uri.outputStream().bufferedWriter().use { writer ->
                        synchronized(logItems) {
                            logItems.forEach {
                                writer.write(it)
                                writer.newLine()
                            }
                        }
                    }
                    file.toString()
                }
            }

            result
                .onSuccess { savedPath ->
                    context.toast(savedPath, Toast.LENGTH_LONG)
                }
                .onFailure { error ->
                    context.toast(error.message ?: "保存日志失败", Toast.LENGTH_LONG)
                }
        }
    }

    fun saveLog() = savePressed()

    fun restartPressed() = reboot()
}
