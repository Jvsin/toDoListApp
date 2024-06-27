package com.example.todolistapp

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.Spinner
import androidx.appcompat.widget.SearchView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.todolistapp.TodoAdapter
import com.example.todolistapp.entities.TodoDatabase
import com.example.todolistapp.databinding.ActivityMainBinding
import com.example.todolistapp.entities.Todo
import com.example.todolistapp.TodoViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationManagerCompat

class MainActivity : AppCompatActivity(), TodoAdapter.TodoClickListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var database: TodoDatabase
    lateinit var viewModel: TodoViewModel
    lateinit var adapter: TodoAdapter

    private var hideFinishedTasks = false
    private var selectedCategory = 0

    private var notificationTime: Int = 1
    private var notificationsTimeTable: IntArray = intArrayOf(1, 5, 10, 15)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        createNotificationChannel()
        initUI()

        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        ).get(TodoViewModel::class.java)

        //observer do aktualizowania listy w adapterze w przypadku zmiany
        viewModel.allTodo.observe(this) { list ->
            list?.let {
                adapter.updateList(list)
            }
        }

        database = TodoDatabase.getDatabase(this)

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                filterList(query ?: "")
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterList(newText ?: "")
                return false
            }
        })

        binding.fabSettings.setOnClickListener {
            showSettingsDialog()
        }
    }

    private fun initUI() {
        binding.recyclerView.setHasFixedSize(true)
        binding.recyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        adapter = TodoAdapter(this, this)
        binding.recyclerView.adapter = adapter

        // nawiązanie kontaktu z AddTodo i dodanie taska do bazy poprzez viewModel
        val getContent =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val todo = result.data?.getSerializableExtra("todo") as? Todo
                    if (todo != null) {
                        viewModel.insertTodo(todo)
                    }
                }
            }

        binding.fabAddTodo.setOnClickListener {
            val intent = Intent(this, AddTodoActivity::class.java)
            getContent.launch(intent)
        }

    }

    // wykrywa kontakt aktywności edycji lub dodawania, po czym aktualizuje taska w bazie danych przy
    // pomocy viewModel
    private val updateOrDeleteTodo =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val todo = result.data?.getSerializableExtra("todo") as Todo
                val isDelete = result.data?.getBooleanExtra("delete_todo", false) as Boolean
                if (todo != null && !isDelete) {
                    viewModel.updateTodo(todo)
                }else if(todo != null && isDelete){
                    viewModel.deleteTodo(todo)
                }
            }
        }


    override fun onItemClicked(todo: Todo) {
        val intent = Intent(this@MainActivity, AddTodoActivity::class.java)
        intent.putExtra("current_todo", todo)
        intent.putExtra("notification_time", notificationsTimeTable[notificationTime])
        updateOrDeleteTodo.launch(intent)
    }

    private fun filterList(query: String) {
        val filteredList = viewModel.allTodo.value?.filter {
            it.title?.contains(query, ignoreCase = true) == true
        }?.sortedBy { parseDate(it.deadline ?: "") }
        adapter.updateList(filteredList ?: emptyList())
    }

    private fun parseDate(dateString: String): Date? {
        val formatter = SimpleDateFormat("EEE, d MMM yyyy HH:mm a", Locale.getDefault())
        return try {
            formatter.parse(dateString)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun showSettingsDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_settings, null)
        val categorySpinner = dialogView.findViewById<Spinner>(R.id.spinner_category)
        val showFinishedCheckBox = dialogView.findViewById<CheckBox>(R.id.checkbox_hide_finished)
        val notificationsSpinner = dialogView.findViewById<Spinner>(R.id.spinner_notifications)

        val categories = resources.getStringArray(R.array.category_array_sorting)
        val categorySpinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        categorySpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = categorySpinnerAdapter

        val notificationsTimes = resources.getStringArray(R.array.notification_times)
        val notsSpinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, notificationsTimes)
        notsSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        notificationsSpinner.adapter = notsSpinnerAdapter

        categorySpinner.setSelection(selectedCategory)
        notificationsSpinner.setSelection(notificationTime)
        showFinishedCheckBox.isChecked = hideFinishedTasks

        AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle("Filtruj zadania")
            .setPositiveButton("OK") { _, _ ->
                selectedCategory = categorySpinner.selectedItemPosition
                hideFinishedTasks = showFinishedCheckBox.isChecked
                notificationTime = notificationsSpinner.selectedItemPosition
                val filteredList = filterTasks(viewModel.allTodo.value ?: emptyList())
                adapter.updateList(filteredList)
            }
            .setNegativeButton("Anuluj", null)
            .create()
            .show()
    }

    private fun filterTasks(list: List<Todo>): List<Todo> {
        var filteredList = list

        if (hideFinishedTasks) {
            filteredList = filteredList.filter { it.isFinished == false}
        }

        if (selectedCategory > 0) {
            filteredList = filteredList.filter { it.category == selectedCategory }
        }

        return filteredList.sortedBy { parseDate(it.deadline ?: "") }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        // Create a notification channel for devices running
        // Android Oreo (API level 26) and above
        val name = "Notify Channel"
        val desc = "A Description of the Channel"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(channelID, name, importance)
        channel.description = desc

        // Get the NotificationManager service and create the channel
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}