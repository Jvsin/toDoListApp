package com.example.todolistapp

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns

class DatabaseTasks(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "tasklist.db"
        const val DATABASE_VERSION = 1
    }

    override fun onCreate(db: SQLiteDatabase) {
        val SQL_CREATE_TASKS_TABLE = "CREATE TABLE " + TaskContract.TaskEntry.TABLE_NAME + " (" +
                TaskContract.TaskEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                TaskContract.TaskEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
                TaskContract.TaskEntry.COLUMN_DESCRIPTION + " TEXT, " +
                TaskContract.TaskEntry.COLUMN_CREATION_TIME + " INTEGER NOT NULL, " +
                TaskContract.TaskEntry.COLUMN_DEADLINE + " INTEGER NOT NULL, " +
                TaskContract.TaskEntry.COLUMN_STATUS + " INTEGER NOT NULL, " +
                TaskContract.TaskEntry.COLUMN_NOTIFICATION_ENABLED + " INTEGER NOT NULL, " +
                TaskContract.TaskEntry.COLUMN_CATEGORY + " TEXT NOT NULL);"
        db.execSQL(SQL_CREATE_TASKS_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS " + TaskContract.TaskEntry.TABLE_NAME)
        onCreate(db)
    }
}

object TaskContract {
    object TaskEntry : BaseColumns {
        const val _ID = BaseColumns._ID
        const val TABLE_NAME = "tasks"
        const val COLUMN_TITLE = "title"
        const val COLUMN_DESCRIPTION = "description"
        const val COLUMN_CREATION_TIME = "creationTime"
        const val COLUMN_DEADLINE = "deadline"
        const val COLUMN_STATUS = "status"
        const val COLUMN_NOTIFICATION_ENABLED = "notificationEnabled"
        const val COLUMN_CATEGORY = "category"
    }

    object AttachmentEntry : BaseColumns {
        const val TABLE_NAME = "attachments"
        const val COLUMN_TASK_ID = "taskId"
        const val COLUMN_TYPE = "type"
        const val COLUMN_URL = "url"
    }
}