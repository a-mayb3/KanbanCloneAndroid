package com.campusaula.edbole.kanban_clone_android.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.MultiAutoCompleteTextView
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
    private lateinit var newProjectName : AutoCompleteTextView
    private lateinit var newProjectDescription : AutoCompleteTextView
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
        returnActionButton.setOnClickListener { finish() }

        newProjectName = findViewById(R.id.newProjectName)
        newProjectDescription = findViewById(R.id.newProjectDescription)

        newProjectCreateButton = findViewById(R.id.newProjectCreateButton)
        newProjectCreateButton.setOnClickListener {
            val projectName = newProjectName.text.toString()
            val projectDescription = newProjectDescription.text.toString()

            if (projectName.isEmpty() || projectDescription.isEmpty()) {
                Toast
                    .makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            lifecycleScope.launch {

                val projectCreate : ProjectCreate = ProjectCreate(projectName, projectDescription)
                val response = api.createProject(projectCreate)

                if (response.isSuccessful){
                    Toast
                        .makeText(this@CreateProjectActivity, "Project created successfully", Toast.LENGTH_SHORT)
                        .show()
                    Log.d("CreateProjectActivity", "Created project: ${response.body()}")
                    startActivity(Intent(this@CreateProjectActivity, MainActivity::class.java))
                } else {
                    Log.e("CreateProjectActivity", "Error creating project: ${response.code()} - ${response.message()}")
                    Toast
                        .makeText(this@CreateProjectActivity, "Error creating project", Toast.LENGTH_SHORT)
                        .show()
                }

            }

        }




    }
}