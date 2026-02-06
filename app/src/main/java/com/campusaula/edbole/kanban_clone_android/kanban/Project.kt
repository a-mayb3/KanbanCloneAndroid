package com.campusaula.edbole.kanban_clone_android.kanban

import com.google.gson.annotations.SerializedName

class Project{
    val id: Int = 0
    val name: String = ""
    val description: String = ""
    val users: List<User> = emptyList()
    val tasks: List<Task> = emptyList()


    override fun toString(): String {
        return "Project(id=$id, name='$name', description='$description', users=$users, tasks=$tasks)"
    }
}

data class ProjectBase(
    @SerializedName("id")
    val id : Int,
    @SerializedName("name")
    val name : String,
    @SerializedName("description")
    val description : String
)

data class ProjectCreate(
    @SerializedName("name")
    val name : String,
    @SerializedName("description")
    val description : String
)
