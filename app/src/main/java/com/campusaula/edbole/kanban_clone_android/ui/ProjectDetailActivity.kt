package com.campusaula.edbole.kanban_clone_android.ui

import android.content.Intent
import android.health.connect.datatypes.units.Percentage
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.campusaula.edbole.kanban_clone_android.R
import com.campusaula.edbole.kanban_clone_android.kanban.Project
import com.campusaula.edbole.kanban_clone_android.kanban.Task
import com.campusaula.edbole.kanban_clone_android.kanban.TaskStatus
import com.campusaula.edbole.kanban_clone_android.network.ApiService
import com.campusaula.edbole.kanban_clone_android.network.RetrofitInstance
import com.campusaula.edbole.kanban_clone_android.ui.adapters.ProjectCollaboratorAdapter
import com.campusaula.edbole.kanban_clone_android.ui.adapters.ProjectTaskAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class ProjectDetailActivity : AppCompatActivity() {

    private lateinit var api: ApiService

    private lateinit var returnActionButton: FloatingActionButton
    private lateinit var addTaskButton: Button
    private lateinit var addCollaboratorButton: Button
    private lateinit var editProjectButton: Button
    private lateinit var deleteProjectButton: Button

    private lateinit var taskListRecycler: RecyclerView
    private lateinit var collaboratorListRecycler: RecyclerView
    private lateinit var collaboratorListAdapter: ProjectCollaboratorAdapter
    private lateinit var taskListAdapter: ProjectTaskAdapter


    private lateinit var projectTitleText : TextView
    private lateinit var projectDescriptionText : TextView
    private lateinit var completedPercentageText: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_project_detail)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        api = RetrofitInstance.getRetrofit(applicationContext).create(ApiService::class.java)
        val projectId = intent.getIntExtra("project_id", -1)

        projectTitleText = findViewById(R.id.projectTitleText)
        projectDescriptionText = findViewById(R.id.projectDescriptionText)
        completedPercentageText = findViewById(R.id.completedPercentageText)

        returnActionButton = findViewById(R.id.returnActionButton)
        returnActionButton.setOnClickListener { finish() }

        addTaskButton = findViewById(R.id.addTaskButton)
        addTaskButton.setOnClickListener {
            val intent = Intent(this, TaskAddActivity::class.java)
            intent.putExtra("project_id", projectId)
            startActivity(intent)
            finish()
        }

        addCollaboratorButton = findViewById(R.id.addCollaboratorButton)
        addCollaboratorButton.setOnClickListener {
            val intent = Intent(this, CollaboratorAddActivity::class.java)
            intent.putExtra("project_id", projectId)
            startActivity(intent)
            finish()
        }

        taskListRecycler = findViewById(R.id.taskListRecycler)
        taskListRecycler.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        taskListAdapter = ProjectTaskAdapter(emptyList(), api, projectId, {
            updateCompletionRate()
        }, {
            finish()
        })
        taskListRecycler.adapter = taskListAdapter

        collaboratorListRecycler = findViewById(R.id.collaboratorListRecycler)
        collaboratorListRecycler.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        collaboratorListAdapter = ProjectCollaboratorAdapter(emptyList(), api, projectId) {
            updateCollaboratorList()
        }
        collaboratorListRecycler.adapter = collaboratorListAdapter

        // Danger Zone buttons
        editProjectButton = findViewById(R.id.editProjectButton)
        editProjectButton.setOnClickListener {
            val intent = Intent(this, ProjectEditActivity::class.java)
            intent.putExtra("project_id", projectId)
            intent.putExtra("project_name", projectTitleText.text.toString())
            intent.putExtra("project_description", projectDescriptionText.text.toString())
            startActivity(intent)
            finish()
        }

        deleteProjectButton = findViewById(R.id.deleteProjectButton)
        deleteProjectButton.setOnClickListener {
            deleteProject(projectId)
        }

        if (projectId > 0) {
            Log.d("ProjectDetailActivity", "Received project ID: $projectId")
            lifecycleScope.launch {
                try {
                    val projectResponse = api.getProjectById(projectId)

                    if (projectResponse.isSuccessful && projectResponse.body() != null) {
                        Log.d("ProjectDetailActivity", "Fetched project: ${projectResponse.body()!!.name}")
                        val project = projectResponse.body()!!


                        Log.d("ProjectDetailActivity", "Displaying project details for: $project")

                        projectTitleText.text = project.name
                        projectDescriptionText.text = project.description

                        var percentageFinished = 0.0;
                        val collaborators = project.users
                        val tasks: List<Task> = project.tasks
                        val totalTasks: Int = tasks.size

                        var completedTasks = 0
                        for (task in tasks) {
                            if (task.status == TaskStatus.COMPLETED) {
                                completedTasks++
                            }
                        }

                        percentageFinished = if (totalTasks > 0) (completedTasks.toDouble() / totalTasks.toDouble()) * 100 else 0.0
                        completedPercentageText.text = "Completed: ${"%.2f".format(percentageFinished)}%"

                        taskListAdapter.submitList(tasks.toMutableList())
                        collaboratorListAdapter.submitList(collaborators.toMutableList())


                    } else {
                        Log.e("ProjectDetailActivity", "Failed to fetch project: ${projectResponse.code()} - ${projectResponse.message()}")
                        finish()
                    }
                } catch (e: Exception) {
                    Log.e("ProjectDetailActivity", "Error fetching project", e)
                    finish()
                }
            }
        }
        else {
            Log.e("ProjectDetailActivity", "No project ID found in intent")
            finish()
        }



    }

    private fun updateCompletionRate() {
        lifecycleScope.launch {
            try {
                val projectId = intent.getIntExtra("project_id", -1)
                val projectResponse = api.getProjectById(projectId)

                if (projectResponse.isSuccessful && projectResponse.body() != null) {
                    val project = projectResponse.body()!!
                    val tasks: List<Task> = project.tasks
                    val totalTasks: Int = tasks.size

                    var completedTasks = 0
                    for (task in tasks) {
                        if (task.status == TaskStatus.COMPLETED) {
                            completedTasks++
                        }
                    }

                    val percentageFinished = if (totalTasks > 0) (completedTasks.toDouble() / totalTasks.toDouble()) * 100 else 0.0
                    completedPercentageText.text = "Completed: ${"%.2f".format(percentageFinished)}%"

                    // Actualizar la lista de tareas también
                    taskListAdapter.submitList(tasks.toMutableList())
                }
            } catch (e: Exception) {
                Log.e("ProjectDetailActivity", "Error updating completion rate", e)
            }
        }
    }

    private fun updateCollaboratorList() {
        lifecycleScope.launch {
            try {
                val projectId = intent.getIntExtra("project_id", -1)
                val projectResponse = api.getProjectById(projectId)

                if (projectResponse.isSuccessful && projectResponse.body() != null) {
                    val project = projectResponse.body()!!
                    val collaborators = project.users

                    collaboratorListAdapter.submitList(collaborators.toMutableList())
                }
            } catch (e: Exception) {
                Log.e("ProjectDetailActivity", "Error updating collaborator list", e)
            }
        }
    }

    private fun deleteProject(projectId: Int) {
        lifecycleScope.launch {
            try {
                Log.d("ProjectDetailActivity", "Deleting project: $projectId")
                val response = api.deleteProject(projectId)

                if (response.isSuccessful) {
                    Log.d("ProjectDetailActivity", "Project deleted successfully")
                    android.widget.Toast.makeText(
                        this@ProjectDetailActivity,
                        "Project deleted successfully",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                    finish()

                    // Volver a MainActivity
                    val intent = Intent(this@ProjectDetailActivity, MainActivity::class.java)
                    startActivity(intent)
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("ProjectDetailActivity", "Error deleting project: $errorBody")
                    android.widget.Toast.makeText(
                        this@ProjectDetailActivity,
                        "Error deleting project: ${response.code()}",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Log.e("ProjectDetailActivity", "Exception deleting project: ${e.message}")
                android.widget.Toast.makeText(
                    this@ProjectDetailActivity,
                    "Failed to delete project: ${e.message}",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}