@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.docscanner.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts.StartIntentSenderForResult
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.docscanner.data.FolderWithCount
import com.example.docscanner.scan.DocumentScannerHelper
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(vm: LibraryViewModel = viewModel()) {
    val folders by vm.folders.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedFolder by remember { mutableStateOf<FolderWithCount?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var renameTarget by remember { mutableStateOf<FolderWithCount?>(null) }

    // The folder a fresh scan should be saved into.
    var scanTargetId by remember { mutableStateOf<Long?>(null) }

    val scannerLauncher = rememberLauncherForActivityResult(StartIntentSenderForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val scanResult = GmsDocumentScanningResult.fromActivityResultIntent(result.data)
            val uris = DocumentScannerHelper.imageUrisFrom(scanResult)
            scanTargetId?.let { vm.addScannedPages(it, uris) }
        }
    }

    fun startScan(folderId: Long) {
        scanTargetId = folderId
        val activity = context.findActivity() ?: return
        DocumentScannerHelper.client()
            .getStartScanIntent(activity)
            .addOnSuccessListener { intentSender ->
                scannerLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, e.message ?: "Scanner unavailable", Toast.LENGTH_SHORT).show()
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("DocScanner Library", fontWeight = FontWeight.SemiBold) })
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showCreateDialog = true },
                icon = { Icon(Icons.Default.CreateNewFolder, contentDescription = null) },
                text = { Text("New folder") }
            )
        }
    ) { padding ->
        if (folders.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No folders yet. Tap \"New folder\" to start.")
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(150.dp),
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(folders, key = { it.id }) { folder ->
                    FolderCard(folder, onClick = { selectedFolder = folder })
                }
            }
        }
    }

    // ----- "More" actions sheet for a tapped folder -----
    selectedFolder?.let { folder ->
        ModalBottomSheet(onDismissRequest = { selectedFolder = null }) {
            Text(
                folder.name,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(start = 20.dp, top = 4.dp, bottom = 8.dp)
            )
            ActionRow(Icons.Default.DocumentScanner, "Scan / Import") {
                selectedFolder = null; startScan(folder.id)
            }
            ActionRow(Icons.Default.PictureAsPdf, "Create PDF") {
                selectedFolder = null
                scope.launch { share(context, vm.exportPdf(folder.id, folder.name), "application/pdf") }
            }
            ActionRow(Icons.Default.FolderZip, "Create ZIP") {
                selectedFolder = null
                scope.launch { share(context, vm.exportZip(folder.id, folder.name), "application/zip") }
            }
            ActionRow(Icons.Default.TextSnippet, "Create TXT (OCR)") {
                selectedFolder = null
                scope.launch { share(context, vm.exportTxt(folder.id, folder.name), "text/plain") }
            }
            ActionRow(Icons.Default.Edit, "Rename") {
                renameTarget = folder; selectedFolder = null
            }
            ActionRow(Icons.Default.Delete, "Delete") {
                vm.deleteFolder(folder.id); selectedFolder = null
            }
            Spacer(Modifier.height(24.dp))
        }
    }

    if (showCreateDialog) {
        TextPromptDialog(
            title = "New folder",
            initial = "",
            onConfirm = { vm.createFolder(it); showCreateDialog = false },
            onDismiss = { showCreateDialog = false }
        )
    }

    renameTarget?.let { folder ->
        TextPromptDialog(
            title = "Rename folder",
            initial = folder.name,
            onConfirm = { vm.renameFolder(folder.id, it); renameTarget = null },
            onDismiss = { renameTarget = null }
        )
    }
}

@Composable
private fun FolderCard(folder: FolderWithCount, onClick: () -> Unit) {
    Card(onClick = onClick, shape = RoundedCornerShape(14.dp)) {
        Column(Modifier.padding(16.dp).fillMaxWidth()) {
            Icon(Icons.Default.Folder, contentDescription = null, modifier = Modifier.size(48.dp))
            Spacer(Modifier.height(12.dp))
            Text(folder.name, fontWeight = FontWeight.SemiBold, maxLines = 1)
            Text("${folder.pageCount} pages", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun ActionRow(icon: ImageVector, label: String, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(label) },
        leadingContent = { Icon(icon, contentDescription = null) },
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    )
}

@Composable
private fun TextPromptDialog(
    title: String,
    initial: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(value = text, onValueChange = { text = it }, singleLine = true)
        },
        confirmButton = { TextButton(onClick = { onConfirm(text) }) { Text("OK") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

/** Shares an exported file via FileProvider, or toasts if export produced nothing. */
private fun share(context: Context, file: File?, mime: String) {
    if (file == null) {
        Toast.makeText(context, "Nothing to export (folder has no pages).", Toast.LENGTH_SHORT).show()
        return
    }
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = mime
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Share ${file.name}"))
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is android.content.ContextWrapper -> baseContext.findActivity()
    else -> null
}