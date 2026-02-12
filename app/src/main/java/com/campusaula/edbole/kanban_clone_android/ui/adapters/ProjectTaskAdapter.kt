package com.campusaula.edbole.kanban_clone_android.ui.adapters

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.campusaula.edbole.kanban_clone_android.R
import com.campusaula.edbole.kanban_clone_android.kanban.Task
import com.campusaula.edbole.kanban_clone_android.kanban.TaskStatus
import com.campusaula.edbole.kanban_clone_android.kanban.TaskUpdate
import com.campusaula.edbole.kanban_clone_android.network.ApiService
import com.campusaula.edbole.kanban_clone_android.ui.TaskEditActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProjectTaskAdapter(
    private var tasks: List<Task>,
    private val apiService: ApiService,
    private val projectId: Int,
    private val onTaskUpdated: () -> Unit = {},
    private val onEditTask: () -> Unit = {}
) : RecyclerView.Adapter<ProjectTaskAdapter.ViewHolder>() {

    private val adapterScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_task, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(tasks[position])
    }

    override fun getItemCount(): Int = tasks.size

    fun submitList(newList: List<Task>) {
        tasks = newList
        notifyDataSetChanged()
    }

    // Cancelar el CoroutineScope cuando el adaptador ya no sea necesario
    fun onDestroy() {
        adapterScope.cancel()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val taskTitleText: TextView = itemView.findViewById(R.id.taskTitleText)
        private val taskDescriptionText: TextView = itemView.findViewById(R.id.taskDescriptionText)
        private val editTaskButton: Button = itemView.findViewById(R.id.editTaskButton)
        private val taskStatusPicker: Spinner = itemView.findViewById(R.id.taskStatusDropdown)

        fun bind(task: Task) {
            taskTitleText.text = task.title
            taskDescriptionText.text = task.description

            editTaskButton.setOnClickListener {
                val intent = Intent(itemView.context, TaskEditActivity::class.java)
                intent.putExtra("project_id", projectId)
                intent.putExtra("task_id", task.id)
                intent.putExtra("task_title", task.title)
                intent.putExtra("task_description", task.description)
                intent.putExtra("task_status", task.status.name)
                itemView.context.startActivity(intent)
                onEditTask() // Cerrar la actividad
            }

            taskStatusPicker.setSelection(TaskStatus.entries.indexOf(task.status))

            taskStatusPicker.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val selectedStatus: TaskStatus = TaskStatus.entries[position]
                    task.status = selectedStatus

                    adapterScope.launch {
                        try {
                            Log.d("ProjectTaskAdapter", "Sending PUT request for task: ${task.id}")
                            Log.d("ProjectTaskAdapter", "Project ID: $projectId")
                            Log.d("ProjectTaskAdapter", "TaskUpdate data: ${TaskUpdate(null, null, task.status)}")

                            val response = apiService.updateProjectTask(
                                projectId,
                                task.id,
                                TaskUpdate(null, null, task.status)
                            )

                            if (response.isSuccessful) {
                                Log.d("ProjectTaskAdapter", "Task updated successfully: ${task.id}")
                                withContext(Dispatchers.Main) {
                                    onTaskUpdated()
                                    notifyDataSetChanged()
                                }
                            } else {
                                val errorBody = response.errorBody()?.string()
                                Log.e("ProjectTaskAdapter", "Error response: $errorBody")
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(
                                        itemView.context,
                                        "Error updating task: ${response.code()} - $errorBody",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("ProjectTaskAdapter", "Exception updating task: ${e.message}")
                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    itemView.context,
                                    "Failed to update task: ${e.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    // No action needed
                }
            }

        }
    }
}
