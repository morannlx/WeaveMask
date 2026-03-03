package com.topjohnwu.magisk.dialog

import android.view.LayoutInflater
import android.widget.TextView
import androidx.annotation.CallSuper
import com.topjohnwu.magisk.R
import com.topjohnwu.magisk.core.di.ServiceLocator
import com.topjohnwu.magisk.events.DialogBuilder
import com.topjohnwu.magisk.view.MagiskDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import com.topjohnwu.magisk.core.R as CoreR

/**
 * Markdown 对话框基类
 * 用于显示 Markdown 格式的文本内容，如更新日志等
 */
abstract class MarkDownDialog : DialogBuilder {

    /**
     * 异步获取 Markdown 文本内容
     * 子类需要实现此方法以提供要显示的 Markdown 内容
     */
    abstract suspend fun getMarkdownText(): String

    /**
     * 构建对话框
     * 使用 dialogScope 执行异步操作，避免 Activity 生命周期问题导致的崩溃
     */
    @CallSuper
    override fun build(dialog: MagiskDialog) {
        with(dialog) {
            val view = LayoutInflater.from(context).inflate(R.layout.markdown_window_md2, null)
            setView(view)
            val tv = view.findViewById<TextView>(R.id.md_txt)

            // 使用对话框自身的协程作用域，避免依赖 Activity 的生命周期
            // 这样可以防止在 Activity 被销毁时出现空指针异常或崩溃
            dialogScope.launch {
                try {
                    val text = withContext(Dispatchers.IO) { getMarkdownText() }
                    // 检查对话框是否仍然显示，避免在对话框关闭后更新 UI
                    if (isShowing) {
                        ServiceLocator.markwon.setMarkdown(tv, text)
                    }
                } catch (e: Exception) {
                    // 捕获所有异常，防止崩溃
                    Timber.e(e)
                    if (isShowing) {
                        tv.setText(CoreR.string.download_file_error)
                    }
                }
            }
        }
    }
}
