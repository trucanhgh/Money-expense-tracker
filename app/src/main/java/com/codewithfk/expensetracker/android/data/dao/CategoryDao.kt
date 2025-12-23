package com.codewithfk.expensetracker.android.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.codewithfk.expensetracker.android.data.model.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {

    // Scoped by ownerId
    @Query("SELECT * FROM category_table WHERE ownerId = :userId ORDER BY name")
    fun getAllCategories(userId: String): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM category_table WHERE ownerId = :userId AND lower(trim(name)) = lower(trim(:name)) LIMIT 1")
    suspend fun getCategoryByName(userId: String, name: String): CategoryEntity?

    @Query("SELECT * FROM category_table WHERE ownerId = :userId AND id = :id LIMIT 1")
    suspend fun getCategoryById(userId: String, id: Int): CategoryEntity?

    // New: fetch all auto-enabled categories for processing (suspend, one-shot)
    @Query("SELECT * FROM category_table WHERE ownerId = :userId AND isAutoTransactionEnabled = 1")
    suspend fun getAutoEnabledCategories(userId: String): List<CategoryEntity>

    @Insert
    suspend fun insertCategory(categoryEntity: CategoryEntity)

    @Delete
    suspend fun deleteCategory(categoryEntity: CategoryEntity)

    @Update
    suspend fun updateCategory(categoryEntity: CategoryEntity)
}
