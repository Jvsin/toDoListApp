package com.example.todolistapp.entities

import androidx.lifecycle.LiveData

class TodoRepository(private val todoDao: TodoDao) {

    val allTodos: LiveData<List<Todo>> = todoDao.getAllTodos()

    suspend fun insert(todo: Todo){
        todoDao.insert(todo)
    }

    suspend fun delete(todo: Todo){
        todoDao.delete(todo)
    }

    suspend fun update(todo: Todo){
        todoDao.update(todo.id, todo.title, todo.note, todo.deadline, todo.category, todo.isFinished,
            todo.notifications, todo.attachments)
    }
//    suspend fun update(todo: Todo){
//        todoDao.update(todo.id, todo.title, todo.note, todo.deadline, todo.category, todo.isFinished,
//            todo.notifications)
//    }
}