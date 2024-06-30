package com.example.todolistapp.entities

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface TodoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(todo: Todo)

    @Delete
    suspend fun delete(todo: Todo)

    @Query("SELECT * from todo_table order by id ASC")
    fun getAllTodos(): LiveData<List<Todo>>

//    @Query("UPDATE todo_table set title = :title, note = :note, deadline = :deadline, " +
//            "category = :category, isFinished = :isFinished, notifications = :notifications where id = :id")
//    suspend fun update(id: Int?, title: String?, note: String?, deadline: String?,
//                       category: Int?, isFinished: Boolean?, notifications: Boolean?): Int

    @Query("UPDATE todo_table set title = :title, note = :note, deadline = :deadline, " +
            "category = :category, isFinished = :isFinished, notifications = :notifications, attachments= :attachments where id = :id")
    suspend fun update(id: Int?, title: String?, note: String?, deadline: String?,
                       category: Int?, isFinished: Boolean?, notifications: Boolean?, attachments: String?): Int

}
