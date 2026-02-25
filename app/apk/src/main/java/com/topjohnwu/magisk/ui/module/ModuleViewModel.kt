package com.topjohnwu.magisk.ui.module

import android.net.Uri
import androidx.databinding.Bindable
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.topjohnwu.magisk.BR
import com.topjohnwu.magisk.MainDirections
import com.topjohnwu.magisk.R
import com.topjohnwu.magisk.arch.AsyncLoadViewModel
import com.topjohnwu.magisk.core.Const
import com.topjohnwu.magisk.core.Info
import com.topjohnwu.magisk.core.base.ContentResultCallback
import com.topjohnwu.magisk.core.model.module.LocalModule
import com.topjohnwu.magisk.core.model.module.OnlineModule
import com.topjohnwu.magisk.databinding.MergeObservableList
import com.topjohnwu.magisk.databinding.RvItem
import com.topjohnwu.magisk.databinding.bindExtra
import com.topjohnwu.magisk.databinding.diffList
import com.topjohnwu.magisk.databinding.set
import com.topjohnwu.magisk.dialog.LocalModuleInstallDialog
import com.topjohnwu.magisk.dialog.OnlineModuleInstallDialog
import com.topjohnwu.magisk.events.GetContentEvent
import com.topjohnwu.magisk.events.SnackbarEvent
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.parcelize.Parcelize
import com.topjohnwu.magisk.core.R as CoreR

enum class ModuleSortMode {
    NAME,
    ENABLED_FIRST,
    UPDATE_FIRST
}

data class ModuleUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val query: String = "",
    val sortMode: ModuleSortMode = ModuleSortMode.NAME,
    val modules: List<LocalModuleRvItem> = emptyList(),
    val errorMessage: String? = null
)

class ModuleViewModel : AsyncLoadViewModel() {

    val bottomBarBarrierIds = intArrayOf(R.id.module_update, R.id.module_remove)

    private val itemsInstalled = diffList<LocalModuleRvItem>()

    val items = MergeObservableList<RvItem>()
    val extraBindings = bindExtra {
        it.put(BR.viewModel, this)
    }

    val data get() = uri

    @get:Bindable
    var loading = true
        private set(value) = set(value, field, { field = it }, BR.loading)

    private val _uiState = MutableStateFlow(ModuleUiState())
    val uiState: StateFlow<ModuleUiState> = _uiState.asStateFlow()

    private var allModules: List<LocalModuleRvItem> = emptyList()

    fun setQuery(query: String) {
        _uiState.update { it.copy(query = query) }
        publishFilteredModules()
    }

    fun setSortMode(sortMode: ModuleSortMode) {
        _uiState.update { it.copy(sortMode = sortMode) }
        publishFilteredModules()
    }

    fun refresh() {
        viewModelScope.launch {
            loadModules(isInitialLoad = false)
        }
    }

    override suspend fun doLoadWork() {
        loadModules(isInitialLoad = true)
    }

    override fun onNetworkChanged(network: Boolean) = startLoading()

    private suspend fun loadModules(isInitialLoad: Boolean) {
        if (isInitialLoad) {
            loading = true
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        } else {
            _uiState.update { it.copy(isRefreshing = true, errorMessage = null) }
        }

        try {
            val moduleLoaded = Info.env.isActive && withContext(Dispatchers.IO) { LocalModule.loaded() }
            val installed = if (moduleLoaded) {
                withContext(Dispatchers.Default) {
                    LocalModule.installed().map { LocalModuleRvItem(it) }
                }
            } else {
                emptyList()
            }

            allModules = installed
            itemsInstalled.update(installed)
            if (moduleLoaded && items.isEmpty()) {
                items.insertItem(InstallModule)
                    .insertList(itemsInstalled)
            }
            publishFilteredModules(errorMessage = null)

            if (moduleLoaded) {
                loadUpdateInfo()
                publishFilteredModules(errorMessage = null)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            allModules = emptyList()
            itemsInstalled.update(emptyList())
            _uiState.update {
                it.copy(
                    modules = emptyList(),
                    errorMessage = e.message
                )
            }
        } finally {
            loading = false
            _uiState.update { it.copy(isLoading = false, isRefreshing = false) }
        }
    }

    private suspend fun loadInstalled() {
        withContext(Dispatchers.Default) {
            val installed = LocalModule.installed().map { LocalModuleRvItem(it) }
            itemsInstalled.update(installed)
        }
    }

    private suspend fun loadUpdateInfo() {
        withContext(Dispatchers.IO) {
            itemsInstalled.forEach {
                if (it.item.fetch())
                    it.fetchedUpdateInfo()
            }
        }
    }

    private fun publishFilteredModules(errorMessage: String? = _uiState.value.errorMessage) {
        val state = _uiState.value
        val query = state.query.trim()
        var modules = if (query.isEmpty()) {
            allModules
        } else {
            allModules.filter {
                it.item.name.contains(query, ignoreCase = true) ||
                    it.item.id.contains(query, ignoreCase = true) ||
                    it.item.author.contains(query, ignoreCase = true)
            }
        }

        modules = when (state.sortMode) {
            ModuleSortMode.NAME -> modules.sortedBy { it.item.name.lowercase() }
            ModuleSortMode.ENABLED_FIRST -> modules.sortedWith(
                compareByDescending<LocalModuleRvItem> { it.isEnabled }
                    .thenBy { it.item.name.lowercase() }
            )
            ModuleSortMode.UPDATE_FIRST -> modules.sortedWith(
                compareByDescending<LocalModuleRvItem> { it.showUpdate }
                    .thenBy { it.item.name.lowercase() }
            )
        }

        _uiState.update {
            it.copy(
                modules = modules,
                errorMessage = errorMessage
            )
        }
    }

    fun downloadPressed(item: OnlineModule?) =
        if (item != null && Info.isConnected.value == true) {
            withExternalRW { OnlineModuleInstallDialog(item).show() }
        } else {
            SnackbarEvent(CoreR.string.no_connection).publish()
        }

    fun installPressed() = withExternalRW {
        GetContentEvent("application/zip", UriCallback()).publish()
    }

    fun requestInstallLocalModule(uri: Uri, displayName: String) {
        LocalModuleInstallDialog(this, uri, displayName).show()
    }

    @Parcelize
    class UriCallback : ContentResultCallback {
        override fun onActivityResult(result: Uri) {
            uri.value = result
        }
    }

    fun runAction(id: String, name: String) {
        MainDirections.actionActionFragment(id, name).navigate()
    }

    companion object {
        private val uri = MutableLiveData<Uri?>()
    }
}
