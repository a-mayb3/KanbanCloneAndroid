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
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class ProjectDetailActivity : AppCompatActivity() {

    private lateinit var api: ApiService

    private lateinit var returnActionButton: FloatingActionButton
    private lateinit var addTaskButton: Button
    private lateinit var addCollaboratorButton: Button

    private lateinit var collaboratorListRecycler: RecyclerView
//    private lateinit var collaboratorListAdapter: CollaboratorListAdapter

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
//        addTaskButton.setOnClickListener {
//            val intent: Intent = Intent(this, CreateTaskActivity::class.java)
//            intent.putExtra("project_id", projectId)
//            startActivity(intent)
//        }

        addCollaboratorButton = findViewById(R.id.addCollaboratorButton)
//        addCollaboratorButton.setOnClickListener {
//            val intent: Intent = Intent(this, AddCollaboratorActivity::class.java)
//            intent.putExtra("project_id", projectId)
//            startActivity(intent)
//        }

        collaboratorListRecycler = findViewById(R.id.collaboratorListRecycler)
//        collaboratorListAdapter =
        collaboratorListRecycler.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)


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
                        val tasks: List<Task> = project.tasks
                        val totalTasks: Int = tasks.size
                        val perTaskPercentage = if (totalTasks > 0) (1.0 / totalTasks)*100 else 0.0

                        for (task in tasks) {
                            if (task.status == TaskStatus.COMPLETED) {
                                percentageFinished += perTaskPercentage
                            }
                        }
                        completedPercentageText.text = "Completed: ${"%.2f".format(percentageFinished * 100)}%"


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
}