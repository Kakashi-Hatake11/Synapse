package com.example.docscanner.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LibraryDao {

    @Query(
        """
        SELECT f.id AS id, f.name AS name, f.createdAt AS createdAt,
               (SELECT COUNT(*) FROM pages p WHERE p.folderId = f.id) AS pageCount
        FROM folders f
        ORDER BY f.createdAt DESC
        """
    )
    fun observeFolders(): Flow<List<FolderWithCount>>

    @Insert
    suspend fun insertFolder(folder: FolderEntity): Long

    @Query("UPDATE folders SET name = :name WHERE id = :folderId")
    suspend fun renameFolder(folderId: Long, name: String)

    @Query("DELETE FROM folders WHERE id = :folderId")
    suspend fun deleteFolder(folderId: Long)

    @Query("SELECT * FROM pages WHERE folderId = :folderId ORDER BY position ASC")
    suspend fun getPages(folderId: Long): List<PageEntity>

    @Query("SELECT COALESCE(MAX(position), -1) FROM pages WHERE folderId = :folderId")
    suspend fun maxPosition(folderId: Long): Int

    @Insert
    suspend fun insertPages(pages: List<PageEntity>)

    @Query("DELETE FROM pages WHERE folderId = :folderId")
    suspend fun deletePages(folderId: Long)
}