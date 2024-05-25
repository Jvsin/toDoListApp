package com.example.todolistapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val task1 = TaskItem("Tytuł", "Opis")
        val task2 = TaskItem("Tytuł", "Opis")

        val taskList: MutableList<TaskItem> = mutableListOf()
        taskList.add(task1)
        taskList.add(task2)

        val adapter = RecyclerViewAdapter(taskList)
        recyclerView.adapter = adapter
    }
}