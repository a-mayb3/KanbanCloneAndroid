package com.campusaula.edbole.KanbanCloneAndroid.kanban

import com.google.gson.annotations.SerializedName

class Project{
    val id: Int = 0
    val name: String = ""
    val description: String = ""
    val users: List<User> = emptyList()
    val tasks: List<Task> = emptyList()
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
