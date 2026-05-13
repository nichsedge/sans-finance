package com.sans.finance.data.local.dao

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Update
import com.sans.finance.data.local.entity.TagEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {
    @Query("SELECT * FROM tags ORDER BY orderIndex ASC, id ASC")
    fun getAllTags(): Flow<List<TagEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTag(tag: TagEntity): Long

    @Query("SELECT * FROM tags WHERE name = :name LIMIT 1")
    suspend fun getTagByName(name: String): TagEntity?

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateTag(tag: TagEntity)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateTags(tags: List<TagEntity>)

    @Delete
    suspend fun deleteTag(tag: TagEntity)

    @Query("DELETE FROM tags WHERE id NOT IN (SELECT DISTINCT tagId FROM expense_tag_ref)")
    suspend fun deleteOrphanedTags()
}
