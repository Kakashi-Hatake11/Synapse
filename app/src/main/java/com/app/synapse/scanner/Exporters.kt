package com.example.docscanner.export

import android.graphics.BitmapFactory
import android.graphics.pdf.PdfDocument
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/** Combines page images into a multi-page PDF using the built-in PdfDocument. */
object PdfExporter {
    fun export(imageFiles: List<File>, outFile: File): File {
        val doc = PdfDocument()
        var pageNumber = 1
        imageFiles.forEach { file ->
            val bitmap = BitmapFactory.decodeFile(file.absolutePath) ?: return@forEach
            val info = PdfDocument.PageInfo
                .Builder(bitmap.width, bitmap.height, pageNumber++)
                .create()
            val page = doc.startPage(info)
            page.canvas.drawBitmap(bitmap, 0f, 0f, null)
            doc.finishPage(page)
            bitmap.recycle()
        }
        FileOutputStream(outFile).use { doc.writeTo(it) }
        doc.close()
        return outFile
    }
}

/** Bundles page images into a ZIP using the Java standard library. */
object ZipExporter {
    fun export(files: List<File>, outFile: File): File {
        ZipOutputStream(FileOutputStream(outFile).buffered()).use { zip ->
            files.forEach { file ->
                zip.putNextEntry(ZipEntry(file.name))
                file.inputStream().use { it.copyTo(zip) }
                zip.closeEntry()
            }
        }
        return outFile
    }
}

/** Runs on-device OCR on each page (ML Kit Text Recognition) and writes a .txt file. */
object TxtExporter {
    private val recognizer =
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    suspend fun export(imageFiles: List<File>, outFile: File): File {
        val sb = StringBuilder()
        imageFiles.forEachIndexed { index, file ->
            val bitmap = BitmapFactory.decodeFile(file.absolutePath) ?: return@forEachIndexed
            val text = recognize(InputImage.fromBitmap(bitmap, 0))
            sb.append("----- Page ${index + 1} -----\n")
                .append(text)
                .append("\n\n")
            bitmap.recycle()
        }
        outFile.writeText(sb.toString())
        return outFile
    }

    private suspend fun recognize(image: InputImage): String =
        suspendCancellableCoroutine { cont ->
            recognizer.process(image)
                .addOnSuccessListener { cont.resume(it.text) }
                .addOnFailureListener { cont.resumeWithException(it) }
        }
}