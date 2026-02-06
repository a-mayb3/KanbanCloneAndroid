package com.campusaula.edbole.kanban_clone_android.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.campusaula.edbole.kanban_clone_android.R
import com.campusaula.edbole.kanban_clone_android.kanban.Project

class ProjectItemAdapter(
    private var items: List<Project>,
    private val onItemClick: ((Project) -> Unit)? = null
) : RecyclerView.Adapter<ProjectItemAdapter.ViewHolder>() {

    fun submitList(newList: List<Project>) {
        items = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_project, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val project = items[position]
        holder.bind(project)
        holder.itemView.setOnClickListener { onItemClick?.invoke(project) }
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTv: TextView = itemView.findViewById(R.id.projectName)
        private val descTv: TextView = itemView.findViewById(R.id.projectDescription)

        fun bind(project: Project) {
            nameTv.text = project.name
            descTv.text = project.description
        }
    }
}
