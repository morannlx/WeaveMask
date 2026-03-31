package io.github.seyud.weave.ui.module.state

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import io.github.seyud.weave.core.R as CoreR
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

private val MODULE_ARCHIVE_MIME_TYPES = arrayOf(
    "application/zip",
    "application/octet-stream",
)

@Composable
internal fun rememberLocalModulePicker(
    onModulePicked: (Uri, String) -> Unit,
): () -> Unit {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val currentOnModulePicked by rememberUpdatedState(onModulePicked)
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        scope.launch {
            val result = runCatching {
                copyDocumentToCache(
                    context = context,
                    sourceUri = uri,
                    cacheDirectoryName = "module_install",
                    fallbackName = "install.zip",
                )
            }
            result
                .onSuccess { (localUri, displayName) ->
                    currentOnModulePicked(localUri, displayName)
                }
                .onFailure { error ->
                    Toast.makeText(
                        context,
                        error.message ?: context.getString(CoreR.string.failure),
                        Toast.LENGTH_LONG,
                    ).show()
                }
        }
    }

    return remember(launcher) {
        { launcher.launch(MODULE_ARCHIVE_MIME_TYPES) }
    }
}

@Composable
internal fun rememberShortcutIconPicker(
    onIconPicked: (String?) -> Unit,
): () -> Unit {
    val currentOnIconPicked by rememberUpdatedState(onIconPicked)
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri ->
        currentOnIconPicked(uri?.toString())
    }

    return remember(launcher) {
        { launcher.launch("image/*") }
    }
}

private suspend fun copyDocumentToCache(
    context: Context,
    sourceUri: Uri,
    cacheDirectoryName: String,
    fallbackName: String,
): Pair<Uri, String> = withContext(Dispatchers.IO) {
    val originalName = context.contentResolver.query(
        sourceUri,
        arrayOf(OpenableColumns.DISPLAY_NAME),
        null,
        null,
        null,
    )?.use { cursor ->
        if (cursor.moveToFirst()) {
            cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
        } else {
            null
        }
    } ?: fallbackName

    val cacheDir = File(context.cacheDir, cacheDirectoryName).apply {
        deleteRecursively()
        mkdirs()
    }
    val target = File(cacheDir, originalName)
    val input = context.contentResolver.openInputStream(sourceUri)
        ?: throw IOException("Cannot read selected file")
    input.use { source ->
        target.outputStream().use { sink ->
            source.copyTo(sink)
        }
    }

    target.toUri() to originalName
}
