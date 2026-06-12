package com.example.docscanner.scan

import android.net.Uri
import com.google.mlkit.vision.documentscanner.GmsDocumentScanner
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult

/**
 * Thin wrapper around the (free) Google ML Kit Document Scanner API.
 * The viewfinder, edge detection, cropping, filters and shadow/stain
 * removal are all provided by Google Play services.
 */
object DocumentScannerHelper {

    fun client(pageLimit: Int = 30): GmsDocumentScanner {
        val options = GmsDocumentScannerOptions.Builder()
            .setGalleryImportAllowed(true)              // matches the "Import" action
            .setPageLimit(pageLimit)
            .setResultFormats(GmsDocumentScannerOptions.RESULT_FORMAT_JPEG)
            .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_FULL) // ML cleanup
            .build()
        return GmsDocumentScanning.getClient(options)
    }

    /** Extracts the scanned page image URIs from an activity result. */
    fun imageUrisFrom(result: GmsDocumentScanningResult?): List<Uri> =
        result?.pages?.mapNotNull { it.imageUri } ?: emptyList()
}