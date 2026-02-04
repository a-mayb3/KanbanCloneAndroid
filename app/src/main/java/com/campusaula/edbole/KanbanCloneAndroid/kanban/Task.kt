package com.campusaula.edbole.KanbanCloneAndroid.kanban

import com.google.gson.annotations.SerializedName

enum class TaskStatus {
    @SerializedName("PENDING")
    PENDING,
    @SerializedName("IN_PROGRESS")
    IN_PROGRESS,
    @SerializedName("COMPLETED")
    COMPLETED,
    @SerializedName("FAILED")
    FAILED,
    @SerializedName("STASHED")
    STASHED
}

class Task {
    val id: Int = 0
    val title: String = ""
    val description: String = ""
    val status: TaskStatus = TaskStatus.PENDING
    val project: Project? = null
}

data class TaskBase(
    @SerializedName("id")
    val id : Int,

    @SerializedName("title")
    val title : String,

    @SerializedName("description")
    val description : String,

    @SerializedName("status")
    val status: TaskStatus
)
