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
import com.example.todolistapp.Notification

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.NotificationManager
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.NotificationManagerCompat
//import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.io.Files
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.IOException
import java.text.ParseException
import java.nio.file.Files
import android.provider.OpenableColumns

class AddTodoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddTodoBinding
    private lateinit var editToDo: Todo
    private lateinit var actualTodo: Todo
    var isUpdate = false
    private var notificationTime : Int = 0
    private var allFilesList: MutableList<String> = mutableListOf()
    private lateinit var listView: ListView

    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { createFileInInternalStorage(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddTodoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        listView = binding.filesList
        setupListView()

        try {
            editToDo = intent.getSerializableExtra("current_todo") as Todo
            notificationTime = intent.getSerializableExtra("notification_time") as Int
            Log.v("powiadomienia", notificationTime.toString())

            actualTodo = editToDo
            binding.etTitle.setText(editToDo.title)
            binding.etNote.setText(editToDo.note)
            binding.switchIsFinished.isChecked = editToDo.isFinished == true
            binding.switchNotifications.isChecked = editToDo.notifications == true
            editToDo.category?.let { binding.spinnerCategory.setSelection(it) }
            editToDo.deadline?.let {
                val calendar = getCalendarFromDateString(it)
                binding.btnPickDateTime.text = SimpleDateFormat("EEE, d MMM yyyy HH:mm a", Locale.getDefault()).format(calendar.time)
            }
            editToDo.attachments?.let {
                val attachs: MutableList<String> = convertFromGsonToList(editToDo.attachments.toString())
                attachs.forEach {
                    addToFileList(it)
                }
            }
            isUpdate = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
        notificationTime = intent.getSerializableExtra("notification_time") as Int
        if (isUpdate) {
            binding.imgDelete.visibility = View.VISIBLE
        } else {
            binding.imgDelete.visibility = View.INVISIBLE
        }
        notificationTime = intent.getSerializableExtra("notification_time") as Int
        Log.v("powiadomienia", "przy dodaniu nowego: " + notificationTime.toString())
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
                    Todo(editToDo.id, title, todoDescription, formatter.format(Date()), deadline,
                        category, isFinished, notifications, convertFromListToGson(allFilesList))
                } else {
                    Todo(null, title, todoDescription, formatter.format(Date()), deadline,
                        category, isFinished, notifications, convertFromListToGson(allFilesList))
                }
                actualTodo = todo
                if (deadline !== "" && notifications)
                    if (checkNotificationPermissions(this)) {
                        // Schedule a notification
                        scheduleNotification(title, todoDescription)
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
            allFilesList.forEach {
                deleteFile(it)
            }
            val intent = Intent()
            intent.putExtra("todo", editToDo)
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

        binding.etBtnAddAttachment.setOnClickListener {
            getContent.launch("*/*")
        }
    }

    private fun setupListView() {
//        listView.setOnItemClickListener { _, _, position, _ -> openFile(position) }
        listView.setOnItemLongClickListener { _, _, position, _ ->
            handleDelete(allFilesList[position])
            deleteFileList(position)
            true
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

    @SuppressLint("ScheduleExactAlarm")
    private fun scheduleNotification(title: String, message: String) {
        val intent = Intent(applicationContext, Notification::class.java).apply {
            putExtra("current_todo", actualTodo)
            putExtra("notification_time", notificationTime)
        }
        val pendingIntent = PendingIntent.getBroadcast(applicationContext, notificationID, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val time = actualTodo.deadline?.let { getNotificationTime(it) }
        if (time != null && time > System.currentTimeMillis()) {
            Log.v("powiadomienia AM", "Scheduling notification for time: " + getDateTime(time) + " current time: ${getDateTime(System.currentTimeMillis())}")
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                time,
                pendingIntent
            )
        } else {
            Log.e("powiadomienia AM", "Scheduled time is in the past or null.")
            Toast.makeText(this@AddTodoActivity, "Powiadomienie upłynęło", Toast.LENGTH_LONG).show()
        }
    }


    fun checkNotificationPermissions(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val isEnabled = notificationManager.areNotificationsEnabled()

            if (!isEnabled) {
                val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                context.startActivity(intent)

                return false
            }
        } else {
            val areEnabled = NotificationManagerCompat.from(context).areNotificationsEnabled()

            if (!areEnabled) {
                val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                context.startActivity(intent)
                return false
            }
        }
        return true
    }

    private fun getNotificationTime(date: String): Long {
        val formatter = SimpleDateFormat("EEE, d MMM yyyy HH:mm a", Locale.getDefault())
        val calendar = Calendar.getInstance()

        try {
            val parsedDate = formatter.parse(date)
            if (parsedDate != null) {
                calendar.time = parsedDate
                val year = calendar.get(Calendar.YEAR)
                val month = calendar.get(Calendar.MONTH)
                val day = calendar.get(Calendar.DAY_OF_MONTH)
                val hours = calendar.get(Calendar.HOUR_OF_DAY)
                val minutes = calendar.get(Calendar.MINUTE)
                calendar.set(year, month, day, hours, minutes)
                Log.v("powiadomienia", notificationTime.toString())
                return calendar.timeInMillis - notificationTime * 60 * 1000
            }
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return 0
    }

    fun getDateTime(timestampMillis: Long): String {
        val date = java.util.Date(timestampMillis)
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
        return sdf.format(date)
    }

    private fun convertFromListToGson(attachments: MutableList<String>): String {
        Log.v("attach","z listy na json " + Gson().toJson(attachments) )
        return Gson().toJson(attachments)
    }

    private fun convertFromGsonToList(attachmentsJson: String): MutableList<String> {
        val type = object : TypeToken<MutableList<String>>() {}.type
        Log.v("attach", "z json na liste " + Gson().fromJson(attachmentsJson, type))
        return Gson().fromJson(attachmentsJson, type)
    }

    private fun openFile(position: Int) {
        val openIntent = Intent(Intent.ACTION_VIEW)
        openIntent.data = Uri.parse(allFilesList[position])
        startActivity(openIntent)
    }

    private fun updateFileList(fileName: String) {
        allFilesList.add(fileName)
        val adapter = ArrayAdapter(this, R.layout.attach_item, allFilesList)
        listView.adapter = adapter
    }

    private fun addToFileList(path: String) {
        allFilesList.add(path)
        val adapter = ArrayAdapter(this, R.layout.attach_item, allFilesList)
        listView.adapter = adapter
    }

    private fun deleteFileList(position: Int) {
        allFilesList.removeAt(position)
        val adapter = ArrayAdapter(this, R.layout.attach_item, allFilesList)
        listView.adapter = adapter
    }

    private fun createFileInInternalStorage(uri: Uri) {
        try {
            val fileName = getFileNameFromUri(uri)
            val destFile = File(filesDir, fileName)
            copyFileToInternalStorage(uri, destFile)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    @Throws(IOException::class)
    private fun copyFileToInternalStorage(uri: Uri, destFile: File) {
        contentResolver.openInputStream(uri)?.use { inputStream ->
            Files.newOutputStream(destFile.toPath()).use { outputStream ->
                val buffer = ByteArray(1024)
                var length: Int
                while (inputStream.read(buffer).also { length = it } > 0) {
                    outputStream.write(buffer, 0, length)
                }
            }
        }
        val newFileName = "${destFile.nameWithoutExtension}-${System.currentTimeMillis()}.${destFile.extension}"
        destFile.renameTo(File(destFile.parent, newFileName))
        updateFileList(newFileName)
    }

    private fun handleDelete(fileName: String) {
        val destFile = File(filesDir, fileName)
        if (destFile.exists()) {
            destFile.delete()
        }
    }

    @SuppressLint("Range")
    private fun getFileNameFromUri(uri: Uri): String {
        return contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
            } else {
                ""
            }
        } ?: ""
    }
}