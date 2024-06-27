package com.example.todolistapp
import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.DatePicker
import android.widget.Toast
import androidx.compose.material3.DatePickerDialog
import com.example.todolistapp.databinding.ActivityAddTodoBinding
import com.example.todolistapp.entities.Todo
import java.text.SimpleDateFormat
import java.util.*

class AddTodoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddTodoBinding
    private lateinit var todo: Todo
    private lateinit var oldTodo: Todo
    var isUpdate = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddTodoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        try {
            oldTodo = intent.getSerializableExtra("current_todo") as Todo
            binding.etTitle.setText(oldTodo.title)
            binding.etNote.setText(oldTodo.note)
            binding.switchIsFinished.isChecked = oldTodo.isFinished == true
            binding.switchNotifications.isChecked = oldTodo.notifications == true
            oldTodo.category?.let { binding.spinnerCategory.setSelection(it) }
            oldTodo.deadline?.let {
                val calendar = getCalendarFromDateString(it)
                binding.btnPickDateTime.text = SimpleDateFormat("EEE, d MMM yyyy HH:mm a", Locale.getDefault()).format(calendar.time)
            }
            isUpdate = true
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (isUpdate) {
            binding.imgDelete.visibility = View.VISIBLE
        } else {
            binding.imgDelete.visibility = View.INVISIBLE
        }

        binding.imgCheck.setOnClickListener {
            val title = binding.etTitle.text.toString()
            val todoDescription = binding.etNote.text.toString()
            val category = binding.spinnerCategory.selectedItemPosition
            val isFinished = binding.switchIsFinished.isChecked
            val notifications = binding.switchNotifications.isChecked
            val deadline = binding.btnPickDateTime.text.toString()

            if (title.isNotEmpty() && todoDescription.isNotEmpty()) {
                val formatter = SimpleDateFormat("EEE, d MMM yyyy HH:mm a")
                val todo = if (isUpdate) {
                    Todo(oldTodo.id, title, todoDescription, formatter.format(Date()), deadline, category, isFinished, notifications)
                } else {
                    Todo(null, title, todoDescription, formatter.format(Date()), deadline, category, isFinished, notifications)
                }
                val intent = Intent()
                intent.putExtra("todo", todo)
                setResult(Activity.RESULT_OK, intent)
                finish()
            } else {
                Toast.makeText(this@AddTodoActivity, "Wpisz nazwę zadania", Toast.LENGTH_LONG).show()
            }
        }

        binding.imgDelete.setOnClickListener {
            val intent = Intent()
            intent.putExtra("todo", oldTodo)
            intent.putExtra("delete_todo", true)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }

        binding.imgBackArrow.setOnClickListener {
            onBackPressed()
        }

        binding.btnPickDateTime.setOnClickListener {
            showDateTimePickerDialog()
        }
    }

    private fun showDateTimePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val timePickerDialog = TimePickerDialog(
                    this,
                    { _, selectedHour, selectedMinute ->
                        calendar.set(selectedYear, selectedMonth, selectedDay, selectedHour, selectedMinute)
                        updateDateTime(calendar.time)
                    },
                    hour,
                    minute,
                    true
                )
                timePickerDialog.show()
            },
            year,
            month,
            day
        )
        datePickerDialog.show()
    }

    private fun updateDateTime(dateTime: Date) {
        val dateFormat = SimpleDateFormat("EEE, d MMM yyyy HH:mm a", Locale.getDefault())
        binding.btnPickDateTime.text = dateFormat.format(dateTime)
    }


    private fun getCalendarFromDateString(dateString: String): Calendar {
        val calendar = Calendar.getInstance()
        val formatter = SimpleDateFormat("EEE, d MMM yyyy HH:mm a")
        calendar.time = formatter.parse(dateString) ?: Date()
        return calendar
    }

    private fun getDateStringFromDatePicker(datePicker: DatePicker): String {
        val calendar = Calendar.getInstance()
        calendar.set(datePicker.year, datePicker.month, datePicker.dayOfMonth)
        val formatter = SimpleDateFormat("EEE, d MMM yyyy HH:mm a")
        return formatter.format(calendar.time)
    }


}