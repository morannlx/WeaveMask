package com.topjohnwu.magisk.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.topjohnwu.magisk.core.R as CoreR

/**
 * 设置页面 Fragment
 * 
 * 注意：设置页面现在由 MainActivity 的 Compose 实现。
 * 这个 Fragment 保留用于兼容旧版导航，但显示为空。
 */
class SettingsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // 返回一个空视图，因为设置页面已经在 MainActivity 的 Compose 中实现
        return View(requireContext())
    }

    override fun onStart() {
        super.onStart()
        activity?.title = resources.getString(CoreR.string.settings)
    }
}
