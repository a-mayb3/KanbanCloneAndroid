package com.campusaula.edbole.kanban_clone_android.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.campusaula.edbole.kanban_clone_android.R
import com.campusaula.edbole.kanban_clone_android.network.ApiService
import com.campusaula.edbole.kanban_clone_android.network.RetrofitInstance
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class CollaboratorAddActivity : AppCompatActivity() {

    private lateinit var api: ApiService

    private lateinit var returnActionButton: FloatingActionButton
    private lateinit var collaboratorEmailInput: EditText
    private lateinit var addCollaboratorButton: Button

    private var projectId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_collaborator_add)
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
        collaboratorEmailInput = findViewById(R.id.collaboratorEmailInput)
        addCollaboratorButton = findViewById(R.id.addCollaboratorButton)

        // Set up button listeners
        returnActionButton.setOnClickListener {
            finish()

            val intent = Intent(this@CollaboratorAddActivity, ProjectDetailActivity::class.java)
            intent.putExtra("project_id", projectId)
            startActivity(intent)

        }

        addCollaboratorButton.setOnClickListener {
            addCollaborator()
        }
    }

    private fun addCollaborator() {
        val user_email = collaboratorEmailInput.text.toString().trim()

        // Validate email
        if (user_email.isEmpty()) {
            Toast.makeText(this, "Email cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(user_email).matches()) {
            Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                Log.d("CollaboratorAddActivity", "Adding collaborator: $user_email")
                val emailBody = mapOf("user_email" to user_email)
                val response = api.addProjectCollaborator(projectId, emailBody)

                if (response.isSuccessful) {
                    Log.d("CollaboratorAddActivity", "Collaborator added successfully")
                    Toast.makeText(
                        this@CollaboratorAddActivity,
                        "Collaborator added successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                    setResult(RESULT_OK)
                    finish()

                    // Reopen ProjectDetailActivity to show the new collaborator
                    val intent = Intent(this@CollaboratorAddActivity, ProjectDetailActivity::class.java)
                    intent.putExtra("project_id", projectId)
                    startActivity(intent)
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("CollaboratorAddActivity", "Error adding collaborator: $errorBody")
                    Toast.makeText(
                        this@CollaboratorAddActivity,
                        "Error adding collaborator: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Log.e("CollaboratorAddActivity", "Exception adding collaborator: ${e.message}")
                Toast.makeText(
                    this@CollaboratorAddActivity,
                    "Failed to add collaborator: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}

