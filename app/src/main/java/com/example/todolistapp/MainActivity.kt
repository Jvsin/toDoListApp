package com.example.todolistapp

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {

    val taskList: MutableList<TaskItem> = mutableListOf()
    var adapter = RecyclerViewAdapter(taskList)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = RecyclerViewAdapter(taskList)
        recyclerView.adapter = adapter

        val task1 = TaskItem("Tytuł", "Opis")
        val task2 = TaskItem("Tytuł", "Opis")

        taskList.add(task1)
        taskList.add(task2)

        val addBtn: FloatingActionButton = findViewById(R.id.floatingButton)
        addBtn.setOnClickListener { view ->
//            Snackbar.make(view, "Test", Snackbar.LENGTH_LONG)
//                .setAction("Action", null).show()
            addTask(this)
        }
    }

    private fun addTask(context: Context) {
        val builder = AlertDialog.Builder(context)
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.dialog_add_task, null)

        val title = view.findViewById<EditText>(R.id.title)
        val description = view.findViewById<EditText>(R.id.description)
//        val status = view.findViewById<Switch>(R.id.status)
//        val notification = view.findViewById<Switch>(R.id.notification)
//        val category = view.findViewById<EditText>(R.id.category)
//        val attachments = view.findViewById<EditText>(R.id.attachments)

        builder.setView(view)
        builder.setPositiveButton("Dodaj") { dialog, _ ->
            val task = TaskItem(
                title = title.text.toString(),
                description = description.text.toString(),
//                status = if (status.isChecked) "zakończone" else "niezakończone",
//                notification = if (notification.isChecked) "włączone" else "wyłączone",
//                category = category.text.toString(),
//                attachments = attachments.text.toString()
            )
            taskList.add(task)
            adapter.notifyDataSetChanged()
            Snackbar.make(view, "Dodano nowe zadanie", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
            dialog.dismiss()
        }

        builder.setNegativeButton("Anuluj") { dialog, _ ->
            dialog.cancel()
        }

        builder.create().show()
    }
}