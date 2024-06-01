package com.example.todolistapp

import android.app.AlertDialog
import android.content.Context
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.EditText
import android.widget.ImageView
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
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

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

        val addBtn: FloatingActionButton = findViewById(R.id.floatingButton)
        addBtn.setOnClickListener { view ->
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

        val category = view.findViewById<Spinner>(R.id.category)
        var selectedCategory: Categories = Categories.OTHERS
        category.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val check = parent.getItemAtPosition(position).toString()
                selectedCategory = when (check) {
                    "Studia" -> Categories.SCHOOL
                    "Praca" -> Categories.WORK
                    "Dom" -> Categories.HOME
                    else -> Categories.OTHERS
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                selectedCategory = Categories.OTHERS
            }
        }
//        val attachments = view.findViewById<EditText>(R.id.attachments)
        val dayPicker = view.findViewById<NumberPicker>(R.id.dayPicker)
        val monthPicker = view.findViewById<NumberPicker>(R.id.monthPicker)
        val yearPicker = view.findViewById<NumberPicker>(R.id.yearPicker)
        setDataPicker(dayPicker, monthPicker, yearPicker)

        builder.setView(view)
        builder.setPositiveButton("Dodaj") { dialog, _ ->
            validateDate(dayPicker, monthPicker, yearPicker)
            Log.v("PO WYBRANIU DATY ", "${dayPicker.value} ${monthPicker.value} ${yearPicker.value}")
            val task = TaskItem(
                title = title.text.toString(),
                description = description.text.toString(),
                notificationEnabled = notification.isChecked,
                creationTime = System.currentTimeMillis(),
                deadline = setDateToTimestamp(dayPicker, monthPicker, yearPicker),
                category = selectedCategory,
                status = TaskStatus.INCOMPLETE
//                attachments = attachments.text.toString()
            )
            Log.v("DODANIE TASKA: ", task.toString())
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
            2 -> dayPicker.maxValue = if (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) 29 else 28
            4, 6, 9, 11 -> dayPicker.maxValue = 30
            else -> dayPicker.maxValue = 31
        }

        val today = LocalDate.now()
        val selectedDate = LocalDate.of(yearPicker.value, monthPicker.value, dayPicker.value)

        if (selectedDate.isBefore(today)) {
            dayPicker.value = today.dayOfMonth
            monthPicker.value = today.monthValue
            yearPicker.value = today.year
        }
    }


    private fun setDateToTimestamp(dayPicker: NumberPicker, monthPicker: NumberPicker, yearPicker: NumberPicker): Long{
        val day = dayPicker.value
        val month = monthPicker.value
        val year = yearPicker.value

        val date = LocalDate.of(year, month, day)
        val zonedDateTime = date.atStartOfDay(ZoneId.systemDefault())
        return zonedDateTime.toInstant().toEpochMilli()
    }

    fun saveFileToExternalStorage(filename: String, fileContent: String) {
        // Sprawdź, czy jest dostępna pamięć zewnętrzna
        if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()) {
            val file = File(Environment.getExternalStorageDirectory(), filename)

            try {
                val outputStream = FileOutputStream(file)
                outputStream.write(fileContent.toByteArray())
                outputStream.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        } else {
            println("Pamięć zewnętrzna nie jest dostępna")
        }
    }
    fun readFileFromExternalStorage(filename: String): String? {
        var fileContent: String? = null

        // Sprawdź, czy jest dostępna pamięć zewnętrzna
        if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState() || Environment.MEDIA_MOUNTED_READ_ONLY == Environment.getExternalStorageState()) {
            val file = File(Environment.getExternalStorageDirectory(), filename)

            try {
                val inputStream = FileInputStream(file)
                fileContent = inputStream.bufferedReader().use { it.readText() }
                inputStream.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        } else {
            println("Pamięć zewnętrzna nie jest dostępna")
        }

        return fileContent
    }

    fun displayImageFromExternalStorage(filename: String, imageView: ImageView) {
        val file = File(Environment.getExternalStorageDirectory(), filename)
        if (file.exists()) {
            val bitmap = BitmapFactory.decodeFile(file.absolutePath)
            imageView.setImageBitmap(bitmap)
        }
    }

}