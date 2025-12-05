package com.codewithfk.expensetracker.android.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.codewithfk.expensetracker.android.data.model.GoalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {

    @Query("SELECT * FROM goal_table WHERE ownerId = :userId ORDER BY id DESC")
    fun getAllGoals(userId: String): Flow<List<GoalEntity>>

    @Query("SELECT * FROM goal_table WHERE ownerId = :userId AND id = :id LIMIT 1")
    suspend fun getGoalById(userId: String, id: Int): GoalEntity?

    @Query("SELECT * FROM goal_table WHERE ownerId = :userId AND lower(trim(name)) = lower(trim(:name)) LIMIT 1")
    suspend fun getGoalByName(userId: String, name: String): GoalEntity?

    @Insert
    suspend fun insertGoal(goal: GoalEntity)

    @Update
    suspend fun updateGoal(goal: GoalEntity)

    @Delete
    suspend fun deleteGoal(goal: GoalEntity)
}
