package com.campusaula.edbole.kanban_clone_android.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.campusaula.edbole.kanban_clone_android.kanban.User

class ProjectCollaboratorAdapter(
    private var collaborators: List<User>
) : RecyclerView.Adapter<ProjectCollaboratorAdapter.ViewHolder>() {

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
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val collaboratorNameText: TextView = itemView.findViewById(R.id.collaboratorNameText)
        private val removeCollaboratorButton: Button = itemView.findViewById(R.id.removeCollaboratorButton)

        fun bind(collaborator: User) {
            collaboratorNameText.text = collaborator.name

        }
    }
    }

}