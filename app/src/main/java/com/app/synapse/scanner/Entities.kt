package com.example.docscanner.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/** A library folder, like "Default Folder" in the screenshots. */
@Entity(tableName = "folders")
data class FolderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis()
)

/** A single scanned page (a JPEG stored in the app's private storage). */
@Entity(tableName = "pages")
data class PageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val folderId: Long,
    val imagePath: String,
    val position: Int,
    val createdAt: Long = System.currentTimeMillis()
)

/** Read-only projection: a folder plus how many pages it contains. */
data class FolderWithCount(
    val id: Long,
    val name: String,
    val createdAt: Long,
    val pageCount: Int
)