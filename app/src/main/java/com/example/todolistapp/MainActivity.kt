package com.example.todolistapp

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.NumberPicker
import android.widget.Spinner
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import java.time.LocalDateTime

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

        val task1 = TaskItem("Tytuł", "Opis", true)
        val task2 = TaskItem("Tytuł", "Opis", true)

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
        val notification = view.findViewById<SwitchCompat>(R.id.notification)
//        val category = view.findViewById<EditText>(R.id.category)
//        val attachments = view.findViewById<EditText>(R.id.attachments)
//        setDataPicker(view)
        val dayPicker = view.findViewById<NumberPicker>(R.id.dayPicker)
        val monthPicker = view.findViewById<NumberPicker>(R.id.monthPicker)
        val yearPicker = view.findViewById<NumberPicker>(R.id.yearPicker)
        setDataPicker(dayPicker, monthPicker, yearPicker)

        builder.setView(view)
        builder.setPositiveButton("Dodaj") { dialog, _ ->
            validateDate(dayPicker, monthPicker, yearPicker)
            val task = TaskItem(
                title = title.text.toString(),
                description = description.text.toString(),
                notificationEnabled = notification.isChecked,
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

    private fun setDataPicker(dayPicker: NumberPicker, monthPicker: NumberPicker, yearPicker: NumberPicker) {
        dayPicker.minValue = 1
        dayPicker.maxValue = 31

        monthPicker.minValue = 1
        monthPicker.maxValue = 12

        yearPicker.minValue = LocalDateTime.now().year
        yearPicker.maxValue = 2100

        monthPicker.setOnValueChangedListener { _, _, _ -> validateDate(dayPicker, monthPicker, yearPicker) }
        yearPicker.setOnValueChangedListener { _, _, _ -> validateDate(dayPicker, monthPicker, yearPicker) }

    }

    private fun validateDate(dayPicker: NumberPicker, monthPicker: NumberPicker, yearPicker: NumberPicker) {
        val month = monthPicker.value
        val year = yearPicker.value

        when (month) {
            // For February, check if the year is a leap year
            2 -> dayPicker.maxValue = if (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) 29 else 28
            // For April, June, September and November, set max day to 30
            4, 6, 9, 11 -> dayPicker.maxValue = 30
            // For the rest of the months, set max day to 31
            else -> dayPicker.maxValue = 31
        }
    }
}