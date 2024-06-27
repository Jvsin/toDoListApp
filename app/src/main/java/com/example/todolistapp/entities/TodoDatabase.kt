package com.example.todolistapp.entities

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = arrayOf(Todo::class), version = 2)
abstract class TodoDatabase : RoomDatabase() {
    abstract fun getTodoDao(): TodoDao

    companion object {
        @Volatile
        private var INSTANCE: TodoDatabase? = null

        fun getDatabase(context: Context): TodoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TodoDatabase::class.java,
                    "tasks"
                )
//                    .addMigrations(MIGRATION_1_2)
                    .build()

                INSTANCE = instance
                instance
            }
        }

//        private val MIGRATION_1_2 = object : Migration(1, 2) {
//            override fun migrate(database: SupportSQLiteDatabase) {
//                database.execSQL("ALTER TABLE todo_table ADD COLUMN deadline TEXT")
//                database.execSQL("ALTER TABLE todo_table ADD COLUMN category INTEGER")
//                database.execSQL("ALTER TABLE todo_table ADD COLUMN isFinished INTEGER")
//                database.execSQL("ALTER TABLE todo_table ADD COLUMN notifications INTEGER")
//            }
//        }
    }

}