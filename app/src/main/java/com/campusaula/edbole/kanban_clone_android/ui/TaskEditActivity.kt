package com.campusaula.edbole.kanban_clone_android.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.campusaula.edbole.kanban_clone_android.R
import com.campusaula.edbole.kanban_clone_android.kanban.TaskStatus
import com.campusaula.edbole.kanban_clone_android.kanban.TaskUpdate
import com.campusaula.edbole.kanban_clone_android.network.ApiService
import com.campusaula.edbole.kanban_clone_android.network.RetrofitInstance
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class TaskEditActivity : AppCompatActivity() {

    private lateinit var api: ApiService

    private lateinit var returnActionButton: FloatingActionButton
    private lateinit var taskTitleInput: EditText
    private lateinit var taskDescriptionInput: EditText
    private lateinit var taskStatusSpinner: Spinner
    private lateinit var saveTaskButton: Button
    private lateinit var deleteTaskButton: Button

    private var projectId: Int = -1
    private var taskId: Int = -1
    private var currentTitle: String = ""
    private var currentDescription: String = ""
    private var currentStatus: TaskStatus = TaskStatus.PENDING

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_task_edit)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        api = RetrofitInstance.getRetrofit(applicationContext).create(ApiService::class.java)

        // Get task and project IDs from intent
        projectId = intent.getIntExtra("project_id", -1)
        taskId = intent.getIntExtra("task_id", -1)
        currentTitle = intent.getStringExtra("task_title") ?: ""
        currentDescription = intent.getStringExtra("task_description") ?: ""
        val statusString = intent.getStringExtra("task_status") ?: "PENDING"
        currentStatus = TaskStatus.valueOf(statusString)

        if (projectId == -1 || taskId == -1) {
            Toast.makeText(this, "Error: Invalid task or project ID", Toast.LENGTH_SHORT).show()
            finish()
        }

        // Initialize views
        returnActionButton = findViewById(R.id.returnActionButton)
        taskTitleInput = findViewById(R.id.taskTitleInput)
        taskDescriptionInput = findViewById(R.id.taskDescriptionInput)
        taskStatusSpinner = findViewById(R.id.taskStatusSpinner)
        saveTaskButton = findViewById(R.id.saveTaskButton)
        deleteTaskButton = findViewById(R.id.deleteTaskButton)

        // Populate fields with current task data
        taskTitleInput.setText(currentTitle)
        taskDescriptionInput.setText(currentDescription)
        taskStatusSpinner.setSelection(TaskStatus.entries.indexOf(currentStatus))

        // Set up button listeners
        returnActionButton.setOnClickListener {
            finish()

            val intent = Intent(this@TaskEditActivity, ProjectDetailActivity::class.java)
            intent.putExtra("project_id", projectId)
            startActivity(intent)
        }

        saveTaskButton.setOnClickListener {
            saveTask()
        }

        deleteTaskButton.setOnClickListener {
            deleteTask()
        }
    }

    private fun saveTask() {
        val newTitle = taskTitleInput.text.toString().trim()
        val newDescription = taskDescriptionInput.text.toString().trim()
        val newStatus = TaskStatus.entries[taskStatusSpinner.selectedItemPosition]

        if (newTitle.isEmpty()) {
            Toast.makeText(this, "Title cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                Log.d("TaskEditActivity", "Updating task: $taskId")
                val taskUpdate = TaskUpdate(
                    title = if (newTitle != currentTitle) newTitle else null,
                    description = if (newDescription != currentDescription) newDescription else null,
                    status = if (newStatus != currentStatus) newStatus else null
                )

                val response = api.updateProjectTask(projectId, taskId, taskUpdate)

                if (response.isSuccessful) {
                    Log.d("TaskEditActivity", "Task updated successfully")
                    Toast.makeText(
                        this@TaskEditActivity,
                        "Task updated successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                    setResult(RESULT_OK)
                    finish()
                    val intent = Intent(this@TaskEditActivity, ProjectDetailActivity::class.java)
                    intent.putExtra("project_id", projectId)
                    startActivity(intent)
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("TaskEditActivity", "Error updating task: $errorBody")
                    Toast.makeText(
                        this@TaskEditActivity,
                        "Error updating task: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Log.e("TaskEditActivity", "Exception updating task: ${e.message}")
                Toast.makeText(
                    this@TaskEditActivity,
                    "Failed to update task: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun deleteTask() {
        lifecycleScope.launch {
            try {
                Log.d("TaskEditActivity", "Deleting task: $taskId")
                val response = api.deleteProjectTask(projectId, taskId)

                if (response.isSuccessful) {
                    Log.d("TaskEditActivity", "Task deleted successfully")
                    Toast.makeText(
                        this@TaskEditActivity,
                        "Task deleted successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                    setResult(RESULT_OK)
                    finish()

                    // Reopen ProjectDetailActivity after deleting the task
                    val intent = Intent(this@TaskEditActivity, ProjectDetailActivity::class.java)
                    intent.putExtra("project_id", projectId)
                    startActivity(intent)
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("TaskEditActivity", "Error deleting task: $errorBody")
                    Toast.makeText(
                        this@TaskEditActivity,
                        "Error deleting task: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Log.e("TaskEditActivity", "Exception deleting task: ${e.message}")
                Toast.makeText(
                    this@TaskEditActivity,
                    "Failed to delete task: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}

