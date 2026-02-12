package com.campusaula.edbole.kanban_clone_android.ui.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.campusaula.edbole.kanban_clone_android.R
import com.campusaula.edbole.kanban_clone_android.kanban.User
import com.campusaula.edbole.kanban_clone_android.network.ApiService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProjectCollaboratorAdapter(
    private var collaborators: List<User>,
    private val apiService: ApiService,
    private val projectId: Int,
    private val onCollaboratorRemoved: () -> Unit = {}
) : RecyclerView.Adapter<ProjectCollaboratorAdapter.ViewHolder>() {

    private val adapterScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun submitList(newList: List<User>) {
        collaborators = newList.toMutableList()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_collaborator, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(collaborators[position])
    }

    override fun getItemCount(): Int = collaborators.size

    fun onDestroy() {
        adapterScope.cancel("Adapter destroyed")
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val collaboratorNameText: TextView = itemView.findViewById(R.id.collaboratorNameText)
        private val collaboratorEmailText: TextView = itemView.findViewById(R.id.collaboratorEmailText)
        private val removeCollaboratorButton: Button = itemView.findViewById(R.id.removeCollaboratorButton)

        fun bind(collaborator: User) {
            collaboratorNameText.text = collaborator.name
            collaboratorEmailText.text = collaborator.email

            removeCollaboratorButton.setOnClickListener {
                adapterScope.launch {
                    try {
                        Log.d("ProjectCollaboratorAdapter", "Removing collaborator: ${collaborator.id}")
                        val response = apiService.removeProjectCollaborator(projectId, collaborator.id)

                        if (response.isSuccessful) {
                            Log.d("ProjectCollaboratorAdapter", "Collaborator removed successfully: ${collaborator.id}")
                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    itemView.context,
                                    "Collaborator removed: ${collaborator.name}",
                                    Toast.LENGTH_SHORT
                                ).show()
                                onCollaboratorRemoved()
                            }
                        } else {
                            val errorBody = response.errorBody()?.string()
                            Log.e("ProjectCollaboratorAdapter", "Error removing collaborator: $errorBody")
                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    itemView.context,
                                    "Error removing collaborator: ${response.code()}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("ProjectCollaboratorAdapter", "Exception removing collaborator: ${e.message}")
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                itemView.context,
                                "Failed to remove collaborator: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        }
    }

}