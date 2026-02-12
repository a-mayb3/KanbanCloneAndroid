package com.campusaula.edbole.kanban_clone_android.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.campusaula.edbole.kanban_clone_android.R
import com.campusaula.edbole.kanban_clone_android.kanban.ProjectCreate
import com.campusaula.edbole.kanban_clone_android.network.ApiService
import com.campusaula.edbole.kanban_clone_android.network.RetrofitInstance
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class ProjectEditActivity : AppCompatActivity() {

    private lateinit var api: ApiService

    private lateinit var returnActionButton: FloatingActionButton
    private lateinit var projectNameInput: EditText
    private lateinit var projectDescriptionInput: EditText
    private lateinit var saveProjectButton: Button

    private var projectId: Int = -1
    private var currentName: String = ""
    private var currentDescription: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_project_edit)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        api = RetrofitInstance.getRetrofit(applicationContext).create(ApiService::class.java)

        // Get project data from intent
        projectId = intent.getIntExtra("project_id", -1)
        currentName = intent.getStringExtra("project_name") ?: ""
        currentDescription = intent.getStringExtra("project_description") ?: ""

        if (projectId == -1) {
            Toast.makeText(this, "Error: Invalid project ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Initialize views
        returnActionButton = findViewById(R.id.returnActionButton)
        projectNameInput = findViewById(R.id.projectNameInput)
        projectDescriptionInput = findViewById(R.id.projectDescriptionInput)
        saveProjectButton = findViewById(R.id.saveProjectButton)

        // Populate fields with current project data
        projectNameInput.setText(currentName)
        projectDescriptionInput.setText(currentDescription)

        // Set up button listeners
        returnActionButton.setOnClickListener {
            finish()

            val intent = Intent(this@ProjectEditActivity, ProjectDetailActivity::class.java)
            intent.putExtra("project_id", projectId)
            startActivity(intent)
        }

        saveProjectButton.setOnClickListener {
            saveProject()
        }
    }

    private fun saveProject() {
        val newName = projectNameInput.text.toString().trim()
        val newDescription = projectDescriptionInput.text.toString().trim()

        if (newName.isEmpty()) {
            Toast.makeText(this, "Project name cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                Log.d("ProjectEditActivity", "Updating project: $projectId")
                val projectCreate = ProjectCreate(
                    name = newName,
                    description = newDescription
                )

                val response = api.updateProject(projectId, projectCreate)

                if (response.isSuccessful) {
                    Log.d("ProjectEditActivity", "Project updated successfully")
                    Toast.makeText(
                        this@ProjectEditActivity,
                        "Project updated successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                    setResult(RESULT_OK)
                    finish()

                    // Reopen ProjectDetailActivity to show the updated project
                    val intent = Intent(this@ProjectEditActivity, ProjectDetailActivity::class.java)
                    intent.putExtra("project_id", projectId)
                    startActivity(intent)
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("ProjectEditActivity", "Error updating project: $errorBody")
                    Toast.makeText(
                        this@ProjectEditActivity,
                        "Error updating project: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Log.e("ProjectEditActivity", "Exception updating project: ${e.message}")
                Toast.makeText(
                    this@ProjectEditActivity,
                    "Failed to update project: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}

