package com.example.todolistapp

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

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
                val deadlineEdit: EditText = dialogView.findViewById(R.id.deadlineEdit)

                titleEdit.setText(title.text)
                descriptionEdit.setText(description.text)
                deadlineEdit.setText(deadline.text)

                editDialog.setView(dialogView)
                editDialog.setPositiveButton("Zapisz") { _, _ ->
                    title.text = titleEdit.text
                    description.text = descriptionEdit.text
//                    deadline.text = deadlineEdit.text
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

//        override fun onStart() {
//            super.onStart()
//            val dialog = dialog
//            if (dialog != null) {
//                val width = ViewGroup.LayoutParams.MATCH_PARENT
//                val height = ViewGroup.LayoutParams.MATCH_PARENT
//                dialog.window?.setLayout(width, height)
//            }
//        }

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
