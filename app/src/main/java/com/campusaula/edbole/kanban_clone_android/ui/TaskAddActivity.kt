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
import com.campusaula.edbole.kanban_clone_android.kanban.TaskBase
import com.campusaula.edbole.kanban_clone_android.kanban.TaskStatus
import com.campusaula.edbole.kanban_clone_android.network.ApiService
import com.campusaula.edbole.kanban_clone_android.network.RetrofitInstance
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class TaskAddActivity : AppCompatActivity() {

    private lateinit var api: ApiService

    private lateinit var returnActionButton: FloatingActionButton
    private lateinit var taskTitleInput: EditText
    private lateinit var taskDescriptionInput: EditText
    private lateinit var taskStatusSpinner: Spinner
    private lateinit var createTaskButton: Button

    private var projectId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_task_add)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        api = RetrofitInstance.getRetrofit(applicationContext).create(ApiService::class.java)

        // Get project ID from intent
        projectId = intent.getIntExtra("project_id", -1)

        if (projectId == -1) {
            Toast.makeText(this, "Error: Invalid project ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Initialize views
        returnActionButton = findViewById(R.id.returnActionButton)
        taskTitleInput = findViewById(R.id.taskTitleInput)
        taskDescriptionInput = findViewById(R.id.taskDescriptionInput)
        taskStatusSpinner = findViewById(R.id.taskStatusSpinner)
        createTaskButton = findViewById(R.id.createTaskButton)

        // Set default status to PENDING (index 0)
        taskStatusSpinner.setSelection(0)

        // Set up button listeners
        returnActionButton.setOnClickListener {
            finish()

            val intent = Intent(this@TaskAddActivity, ProjectDetailActivity::class.java)
            intent.putExtra("project_id", projectId)
            startActivity(intent)
        }

        createTaskButton.setOnClickListener {
            createTask()
        }
    }

    private fun createTask() {
        val title = taskTitleInput.text.toString().trim()
        val description = taskDescriptionInput.text.toString().trim()
        val status = TaskStatus.entries[taskStatusSpinner.selectedItemPosition]

        if (title.isEmpty()) {
            Toast.makeText(this, "Title cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                Log.d("TaskAddActivity", "Creating task: $title")
                val taskBase = TaskBase(
                    id = 0, // ID will be assigned by the server
                    title = title,
                    description = description,
                    status = status
                )

                val response = api.createTask(projectId, taskBase)

                if (response.isSuccessful) {
                    Log.d("TaskAddActivity", "Task created successfully")
                    Toast.makeText(
                        this@TaskAddActivity,
                        "Task created successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                    setResult(RESULT_OK)
                    finish()

                    // Reopen ProjectDetailActivity to show the new task
                    val intent = Intent(this@TaskAddActivity, ProjectDetailActivity::class.java)
                    intent.putExtra("project_id", projectId)
                    startActivity(intent)
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("TaskAddActivity", "Error creating task: $errorBody")
                    Toast.makeText(
                        this@TaskAddActivity,
                        "Error creating task: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Log.e("TaskAddActivity", "Exception creating task: ${e.message}")
                Toast.makeText(
                    this@TaskAddActivity,
                    "Failed to create task: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}

