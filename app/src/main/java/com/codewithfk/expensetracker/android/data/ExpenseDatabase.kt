package com.codewithfk.expensetracker.android.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.codewithfk.expensetracker.android.data.dao.CategoryDao
import com.codewithfk.expensetracker.android.data.dao.ExpenseDao
import com.codewithfk.expensetracker.android.data.dao.GoalDao
import com.codewithfk.expensetracker.android.data.dao.NotificationDao
import com.codewithfk.expensetracker.android.data.model.CategoryEntity
import com.codewithfk.expensetracker.android.data.model.ExpenseEntity
import com.codewithfk.expensetracker.android.data.model.UserEntity
import com.codewithfk.expensetracker.android.data.model.GoalEntity
import com.codewithfk.expensetracker.android.data.model.NotificationEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@Database(entities = [ExpenseEntity::class, CategoryEntity::class, UserEntity::class, GoalEntity::class, NotificationEntity::class], version = 9, exportSchema = false)
@Singleton
abstract class ExpenseDatabase : RoomDatabase() {

    abstract fun expenseDao(): ExpenseDao
    abstract fun categoryDao(): CategoryDao
    abstract fun userDao(): com.codewithfk.expensetracker.android.data.dao.UserDao
    abstract fun goalDao(): GoalDao
    abstract fun notificationDao(): NotificationDao

    companion object {
        const val DATABASE_NAME = "expense_database"

        @Volatile
        private var INSTANCE: ExpenseDatabase? = null

        fun getInstance(@ApplicationContext context: Context): ExpenseDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ExpenseDatabase::class.java,
                    DATABASE_NAME
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                    .addMigrations(MIGRATION_5_6)
                    .addMigrations(MIGRATION_6_7)
                    .addMigrations(MIGRATION_7_8)
                    .addMigrations(MIGRATION_8_9)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Add new columns to category_table for auto transactions
        db.execSQL("ALTER TABLE category_table ADD COLUMN isAutoTransactionEnabled INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE category_table ADD COLUMN autoAmount REAL NOT NULL DEFAULT 0.0")
        db.execSQL("ALTER TABLE category_table ADD COLUMN autoType TEXT NOT NULL DEFAULT 'Expense'")
        db.execSQL("ALTER TABLE category_table ADD COLUMN autoRepeatType TEXT NOT NULL DEFAULT 'WEEKLY'")
        db.execSQL("ALTER TABLE category_table ADD COLUMN autoDayOfWeek INTEGER")
        db.execSQL("ALTER TABLE category_table ADD COLUMN autoDayOfMonth INTEGER")
        db.execSQL("ALTER TABLE category_table ADD COLUMN lastAutoExecutedDate TEXT")
        // Add note column to expense_table to store optional notes (default empty)
        db.execSQL("ALTER TABLE expense_table ADD COLUMN note TEXT NOT NULL DEFAULT ''")
    }
}

val MIGRATION_7_8 = object : Migration(7, 8) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Safer migration: don't assume an old notification_table exists. Check first.
        val cursor = db.query("SELECT name FROM sqlite_master WHERE type='table' AND name='notification_table'")
        val oldExists = cursor.use { it.count > 0 }

        if (oldExists) {
            // Create new table with the schema Room expects
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS notification_table_new (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    title TEXT NOT NULL,
                    message TEXT NOT NULL,
                    timestamp INTEGER NOT NULL,
                    isRead INTEGER NOT NULL DEFAULT 0,
                    type TEXT NOT NULL DEFAULT '',
                    ownerId TEXT NOT NULL DEFAULT ''
                )
                """.trimIndent()
            )

            // Inspect existing columns in the old table to safely map them when copying
            val colsCursor = db.query("PRAGMA table_info(notification_table)")
            val existingCols = mutableSetOf<String>()
            colsCursor.use {
                while (it.moveToNext()) {
                    val colName = try {
                        it.getString(it.getColumnIndex("name"))
                    } catch (_: Exception) {
                        null
                    }
                    if (!colName.isNullOrEmpty()) existingCols.add(colName)
                }
            }

            // Build SELECT expression using available columns or sensible defaults
            val selId = if (existingCols.contains("id")) "id" else "NULL"
            val selTitle = if (existingCols.contains("title")) "title" else "''"
            val selMessage = if (existingCols.contains("message")) "message" else "''"
            val selTimestamp = if (existingCols.contains("timestamp")) "timestamp" else "0"
            val selIsRead = if (existingCols.contains("isRead")) "IFNULL(isRead, 0)" else "0"
            val selType = if (existingCols.contains("type")) "IFNULL(type, '')" else "''"
            val selOwner = if (existingCols.contains("ownerId")) "IFNULL(ownerId, '')" else "''"

            try {
                db.execSQL(
                    "INSERT INTO notification_table_new (id, title, message, timestamp, isRead, type, ownerId) SELECT $selId, $selTitle, $selMessage, $selTimestamp, $selIsRead, $selType, $selOwner FROM notification_table;"
                )
            } catch (_: Exception) {
                // If copy fails for any reason, don't crash the migration. The new table will be present.
            }

            db.execSQL("DROP TABLE IF EXISTS notification_table;")
            db.execSQL("ALTER TABLE notification_table_new RENAME TO notification_table;")
        } else {
            // Fresh install or older DB without notifications: create the final table directly
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS notification_table (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    title TEXT NOT NULL,
                    message TEXT NOT NULL,
                    timestamp INTEGER NOT NULL,
                    isRead INTEGER NOT NULL DEFAULT 0,
                    type TEXT NOT NULL DEFAULT '',
                    ownerId TEXT NOT NULL DEFAULT ''
                )
                """.trimIndent()
            )
        }
    }
}

val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Add ownerId column to expense_table
        db.execSQL("ALTER TABLE expense_table ADD COLUMN ownerId TEXT NOT NULL DEFAULT ''")
        // Add ownerId column to category_table
        db.execSQL("ALTER TABLE category_table ADD COLUMN ownerId TEXT NOT NULL DEFAULT ''")
        // Add ownerId column to goal_table
        db.execSQL("ALTER TABLE goal_table ADD COLUMN ownerId TEXT NOT NULL DEFAULT ''")
        // Note: user_table remains as-is (local credentials)
    }
}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS expense_table_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                title TEXT NOT NULL,
                amount REAL NOT NULL,
                date TEXT NOT NULL,
                type TEXT NOT NULL
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            INSERT INTO expense_table_new (id, title, amount, date, type)
            SELECT id, title, amount, date, type FROM expense_table
            """.trimIndent()
        )

        db.execSQL("DROP TABLE IF EXISTS expense_table")
        db.execSQL("ALTER TABLE expense_table_new RENAME TO expense_table")
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS category_table (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL
            )
            """.trimIndent()
        )

        // Seed default categories (Vietnamese)
        db.execSQL("INSERT OR IGNORE INTO category_table (name) VALUES ('Khác')")
        db.execSQL("INSERT OR IGNORE INTO category_table (name) VALUES ('Tạp hóa')")
        db.execSQL("INSERT OR IGNORE INTO category_table (name) VALUES ('Tiền thuê')")
        db.execSQL("INSERT OR IGNORE INTO category_table (name) VALUES ('Di chuyển')")
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS user_table (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT NOT NULL,
                password TEXT NOT NULL
            )
            """.trimIndent()
        )
    }
}

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS goal_table (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                targetAmount REAL NOT NULL,
                frequency TEXT,
                reminderEnabled INTEGER NOT NULL
            )
            """.trimIndent()
        )
    }
}

val MIGRATION_8_9 = object : Migration(8, 9) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Add createdAt column to expense_table. Use default 0 for existing rows, then backfill with current time.
        try {
            db.execSQL("ALTER TABLE expense_table ADD COLUMN createdAt INTEGER NOT NULL DEFAULT 0")
            // Backfill existing rows createdAt to current time (milliseconds)
            db.execSQL("UPDATE expense_table SET createdAt = (strftime('%s','now') * 1000) WHERE createdAt = 0")
        } catch (_: Exception) {
            // If migration fails, do not crash; keep best-effort approach
        }
    }
}
