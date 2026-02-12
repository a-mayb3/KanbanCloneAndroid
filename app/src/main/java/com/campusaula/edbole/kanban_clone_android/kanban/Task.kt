package com.campusaula.edbole.kanban_clone_android.kanban

import com.google.gson.annotations.SerializedName

enum class TaskStatus {
    @SerializedName("pending")
    PENDING,
    @SerializedName("in_progress")
    IN_PROGRESS,
    @SerializedName("completed")
    COMPLETED,
    @SerializedName("failed")
    FAILED,
    @SerializedName("stashed")
    STASHED
}

class Task {
    val id: Int = 0
    val title: String = ""
    val description: String = ""
    var status: TaskStatus = TaskStatus.PENDING
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

data class TaskUpdate(
    @SerializedName("title")
    val title : String?,

    @SerializedName("description")
    val description : String?,

    @SerializedName("status")
    val status: TaskStatus?
)
