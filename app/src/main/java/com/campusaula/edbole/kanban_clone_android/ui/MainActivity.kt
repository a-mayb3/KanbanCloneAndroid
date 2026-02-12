package com.campusaula.edbole.kanban_clone_android.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.campusaula.edbole.kanban_clone_android.R
import com.campusaula.edbole.kanban_clone_android.kanban.Project
import com.campusaula.edbole.kanban_clone_android.network.ApiService
import com.campusaula.edbole.kanban_clone_android.network.RetrofitInstance
import com.campusaula.edbole.kanban_clone_android.ui.adapters.ProjectItemAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var api: ApiService
    private lateinit var projectList : List<Project>

    private lateinit var loggedInAs: TextView
    private lateinit var logoutButton: Button
    private lateinit var addProjectActionButton: FloatingActionButton
    private lateinit var projectsRecyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        api = RetrofitInstance.getRetrofit(applicationContext).create(ApiService::class.java)
        projectList = emptyList()

        /* Activity components */
        loggedInAs = findViewById(R.id.loggedInAs)
        logoutButton = findViewById(R.id.logoutButton)
        addProjectActionButton = findViewById(R.id.addProjectActionButton)
        projectsRecyclerView = findViewById(R.id.projectsRecyclerView)
        projectsRecyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        val adapter = ProjectItemAdapter(projectList) { project ->
            val intent = Intent(this, ProjectDetailActivity::class.java)
            intent.putExtra("project_id", project.id)
            startActivity(intent)
        }
        projectsRecyclerView.adapter = adapter

        addProjectActionButton.setOnClickListener {
            val intent = Intent(this, CreateProjectActivity::class.java)
            startActivity(intent)
        }

        /* Getting the logged-in user info */
        lifecycleScope.launch{

            val getMe = api.getMe()
            if (getMe.isSuccessful){
                val user = getMe.body()
                loggedInAs.text = "Logged in as: ${user?.name}"
                projectList = api.getAllProjects().body()!!
                adapter.submitList(projectList)
            } else {
                val intent = Intent(this@MainActivity, LoginActivity::class.java)
                startActivity(intent)
            }

        }

        logoutButton.setOnClickListener {
            lifecycleScope.launch {
                val logoutResponse = api.logout()
                if (logoutResponse.isSuccessful) {
                    // Clear cookies for the API host
                    RetrofitInstance.clearCookiesForHost(
                        "10.0.2.2:8000"
                    )
                    // Navigate back to the login screen
                    val intent =
                        Intent(this@MainActivity, LoginActivity::class.java)
                    startActivity(intent)
                    finish() // Optional: close the MainActivity so it's removed from the back stack
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        "Logout failed",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}