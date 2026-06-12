package com.example.docscanner.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.docscanner.data.AppDatabase
import com.example.docscanner.data.FolderEntity
import com.example.docscanner.data.FolderWithCount
import com.example.docscanner.data.PageEntity
import com.example.docscanner.export.PdfExporter
import com.example.docscanner.export.TxtExporter
import com.example.docscanner.export.ZipExporter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class LibraryViewModel(app: Application) : AndroidViewModel(app) {

    private val dao = AppDatabase.get(app).libraryDao()

    val folders: StateFlow<List<FolderWithCount>> =
        dao.observeFolders()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun createFolder(name: String) = viewModelScope.launch {
        dao.insertFolder(FolderEntity(name = name.ifBlank { "Untitled" }))
    }

    fun renameFolder(folderId: Long, name: String) = viewModelScope.launch {
        if (name.isNotBlank()) dao.renameFolder(folderId, name)
    }

    fun deleteFolder(folderId: Long) = viewModelScope.launch {
        // Remove stored page images, then DB rows.
        withContext(Dispatchers.IO) {
            dao.getPages(folderId).forEach { runCatching { File(it.imagePath).delete() } }
        }
        dao.deletePages(folderId)
        dao.deleteFolder(folderId)
    }

    /** Copies freshly scanned/imported images into private storage and records them. */
    fun addScannedPages(folderId: Long, uris: List<Uri>) = viewModelScope.launch {
        if (uris.isEmpty()) return@launch
        withContext(Dispatchers.IO) {
            val ctx = getApplication<Application>()
            val dir = File(ctx.filesDir, "folder_$folderId").apply { mkdirs() }
            var position = dao.maxPosition(folderId) + 1
            val pages = uris.mapNotNull { uri ->
                val dest = File(dir, "page_${System.currentTimeMillis()}_$position.jpg")
                val ok = runCatching {
                    ctx.contentResolver.openInputStream(uri)?.use { input ->
                        dest.outputStream().use { input.copyTo(it) }
                    }
                }.isSuccess
                if (ok) PageEntity(folderId = folderId, imagePath = dest.absolutePath, position = position++)
                else null
            }
            dao.insertPages(pages)
        }
    }

    suspend fun exportPdf(folderId: Long, folderName: String): File? =
        withFiles(folderId) { files ->
            PdfExporter.export(files, outFile("$folderName.pdf"))
        }

    suspend fun exportZip(folderId: Long, folderName: String): File? =
        withFiles(folderId) { files ->
            ZipExporter.export(files, outFile("$folderName.zip"))
        }

    suspend fun exportTxt(folderId: Long, folderName: String): File? =
        withFiles(folderId) { files ->
            TxtExporter.export(files, outFile("$folderName.txt"))
        }

    private suspend fun withFiles(folderId: Long, block: suspend (List<File>) -> File): File? =
        withContext(Dispatchers.IO) {
            val files = dao.getPages(folderId)
                .map { File(it.imagePath) }
                .filter { it.exists() }
            if (files.isEmpty()) null else block(files)
        }

    private fun outFile(name: String): File {
        val dir = File(getApplication<Application>().getExternalFilesDir(null), "exports")
            .apply { mkdirs() }
        return File(dir, name.replace(Regex("[^A-Za-z0-9._-]"), "_"))
    }
}