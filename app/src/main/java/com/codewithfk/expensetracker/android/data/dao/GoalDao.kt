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

    @Query("SELECT * FROM goal_table ORDER BY id DESC")
    fun getAllGoals(): Flow<List<GoalEntity>>

    @Query("SELECT * FROM goal_table WHERE id = :id LIMIT 1")
    suspend fun getGoalById(id: Int): GoalEntity?

    @Query("SELECT * FROM goal_table WHERE lower(trim(name)) = lower(trim(:name)) LIMIT 1")
    suspend fun getGoalByName(name: String): GoalEntity?

    @Insert
    suspend fun insertGoal(goal: GoalEntity)

    @Update
    suspend fun updateGoal(goal: GoalEntity)

    @Delete
    suspend fun deleteGoal(goal: GoalEntity)
}

