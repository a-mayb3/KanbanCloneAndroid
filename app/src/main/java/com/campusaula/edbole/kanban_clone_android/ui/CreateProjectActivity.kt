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


class CreateProjectActivity : AppCompatActivity() {

    private lateinit var api: ApiService

    private lateinit var returnActionButton : FloatingActionButton
    private lateinit var newProjectName : EditText
    private lateinit var newProjectDescription : EditText
    private lateinit var newProjectCreateButton : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_create_project)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        api = RetrofitInstance.getRetrofit(applicationContext).create(ApiService::class.java)

        returnActionButton = findViewById(R.id.returnActionButton)
        returnActionButton.setOnClickListener {
            finish()

            val intent = Intent(this@CreateProjectActivity, MainActivity::class.java)
            startActivity(intent)
        }

        newProjectName = findViewById(R.id.newProjectName)
        newProjectDescription = findViewById(R.id.newProjectDescription)

        newProjectCreateButton = findViewById(R.id.newProjectCreateButton)
        newProjectCreateButton.setOnClickListener {
            val projectName = newProjectName.text.toString().trim()
            val projectDescription = newProjectDescription.text.toString().trim()

            if (projectName.isEmpty()) {
                Toast.makeText(this, "Project name cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                try {
                    Log.d("CreateProjectActivity", "Creating project: $projectName")
                    val projectCreate = ProjectCreate(projectName, projectDescription)
                    val response = api.createProject(projectCreate)

                    if (response.isSuccessful) {
                        Log.d("CreateProjectActivity", "Project created successfully: ${response.body()}")
                        Toast.makeText(
                            this@CreateProjectActivity,
                            "Project created successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()

                        // Volver a MainActivity para ver el nuevo proyecto
                        val intent = Intent(this@CreateProjectActivity, MainActivity::class.java)
                        startActivity(intent)
                    } else {
                        val errorBody = response.errorBody()?.string()
                        Log.e("CreateProjectActivity", "Error creating project: $errorBody")
                        Toast.makeText(
                            this@CreateProjectActivity,
                            "Error creating project: ${response.code()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: Exception) {
                    Log.e("CreateProjectActivity", "Exception creating project: ${e.message}")
                    Toast.makeText(
                        this@CreateProjectActivity,
                        "Failed to create project: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

    }
}