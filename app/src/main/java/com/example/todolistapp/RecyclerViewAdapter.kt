package com.example.todolistapp

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.NumberPicker
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

interface OnItemClickListener {
    fun onItemClick(item: TaskItem)
}

class RecyclerViewAdapter(private val dataList: List<TaskItem>) : RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>() {
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewTitle: TextView = itemView.findViewById(R.id.title)
        val textViewDescription: TextView = itemView.findViewById(R.id.description)
        val deadlineDay: TextView = itemView.findViewById(R.id.deadline)
        val daysToGo: TextView = itemView.findViewById(R.id.daysToGo)

        init {
            itemView.setOnClickListener {
                val dialog = TaskDialog.newInstance(
                    textViewTitle.text.toString(),
                    textViewDescription.text.toString(),
                    deadlineDay.text.toString(),
                    daysToGo.text.toString()
                )
                dialog.show((itemView.context as AppCompatActivity).supportFragmentManager, "customDialog")
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }
    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = dataList[position]
        holder.textViewTitle.text = currentItem.title
        holder.textViewDescription.text = currentItem.description
        holder.deadlineDay.text = convertFromTimestamp(currentItem.deadline)
        holder.daysToGo.text = countDaysToGo(currentItem.deadline)
    }

    private fun convertFromTimestamp(date: Long): String {
        val instant = Instant.ofEpochMilli(date)
        val zonedDateTime = instant.atZone(ZoneId.systemDefault())
        val formatter = DateTimeFormatter.ofPattern("dd MM yyyy")
        return formatter.format(zonedDateTime)
    }

    private fun countDaysToGo(date: Long): String {
        val now = Instant.now().atZone(ZoneId.systemDefault())
        val targetDate = Instant.ofEpochMilli(date).atZone(ZoneId.systemDefault())
        return ChronoUnit.DAYS.between(now, targetDate).toString()
    }

    class TaskDialog : DialogFragment() {

        private lateinit var title: TextView
        private lateinit var description: TextView
        private lateinit var deadline: TextView
        private lateinit var daysToGo: TextView
        private lateinit var editButton: Button
        private lateinit var confirmButton: Button
        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            val view = inflater.inflate(R.layout.dialog_task_view, container, false)

            title = view.findViewById(R.id.title)
            description = view.findViewById(R.id.description)
            deadline = view.findViewById(R.id.deadline)
            daysToGo = view.findViewById(R.id.daysToGo)
            editButton = view.findViewById(R.id.editButton)
            editButton = view.findViewById(R.id.editButton)

            title.text = arguments?.getString("title")
            description.text = arguments?.getString("description")
            deadline.text = arguments?.getString("deadline")
            daysToGo.text = arguments?.getString("daysToGo")

            editButton.setOnClickListener {
                val editDialog = AlertDialog.Builder(it.context)
                val inflater = LayoutInflater.from(it.context)
                val dialogView = inflater.inflate(R.layout.dialog_edit_task, null)

                val titleEdit: EditText = dialogView.findViewById(R.id.titleEdit)
                val descriptionEdit: EditText = dialogView.findViewById(R.id.descriptionEdit)
//                val deadlineEdit: EditText = dialogView.findViewById(R.id.deadlineEdit)

                val dayPicker = dialogView.findViewById<NumberPicker>(R.id.dayPicker)
                val monthPicker = dialogView.findViewById<NumberPicker>(R.id.monthPicker)
                val yearPicker = dialogView.findViewById<NumberPicker>(R.id.yearPicker)
                setDataPicker(dayPicker, monthPicker, yearPicker)

                titleEdit.setText(title.text)
                descriptionEdit.setText(description.text)
//                deadlineEdit.setText(deadline.text)

                editDialog.setView(dialogView)
                editDialog.setPositiveButton("Zapisz") { _, _ ->
                    title.text = titleEdit.text
                    description.text = descriptionEdit.text
                    deadline.text = setDateToTimestamp(dayPicker, monthPicker, yearPicker).toString()
                }
                editDialog.setNegativeButton("Anuluj") { dialog, _ ->
                    dialog.dismiss()
                }
                editDialog.create().show()
            }

            super.onStart()
            val dialog = dialog
            if (dialog != null) {
                val width = ViewGroup.LayoutParams.MATCH_PARENT
                val height = ViewGroup.LayoutParams.MATCH_PARENT
                dialog.window?.setLayout(width, height)
            }
            return view
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

        companion object {
            fun newInstance(title: String, description: String, deadline: String, daysToGo: String): TaskDialog {
                val fragment = TaskDialog()
                val args = Bundle()
                args.putString("title", title)
                args.putString("description", description)
                args.putString("deadline", deadline)
                args.putString("daysToGo", daysToGo)
                fragment.arguments = args
                return fragment
            }
        }


    }
}
