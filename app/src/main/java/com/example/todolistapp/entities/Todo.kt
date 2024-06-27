package com.example.todolistapp.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "todo_table")
data class Todo(
    @PrimaryKey(autoGenerate = true) val id: Int?,
    @ColumnInfo(name = "title") val title: String?,
    @ColumnInfo(name = "note") val note: String?,
    @ColumnInfo(name = "date") val date: String,
    @ColumnInfo(name = "deadline") val deadline: String?,
    @ColumnInfo(name = "category") val category: Int?,
    @ColumnInfo(name = "isFinished") val isFinished: Boolean?,
    @ColumnInfo(name = "notifications") val notifications: Boolean?,
): java.io.Serializable