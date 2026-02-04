package com.campusaula.edbole.kanban_clone_android.kanban

import com.google.gson.annotations.SerializedName

class User {
    val id: Int = 0
    val name : String = ""
    val email: String = ""
    val password: String = ""
    val projects: List<Project> = emptyList()
}

data class UserBase (
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("email")
    val email: String
)

data class ProjectUser(
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("projects")
    val projects: List<ProjectBase>
)

data class UserLogin (
    @SerializedName("email")
    val email: String,
    @SerializedName("password")
    val password: String
)

data class UserCreate (
    @SerializedName("name")
    val name: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("password")
    val password: String
)

data class UserUpdatePassword(
    @SerializedName("password")
    val password: String,
    @SerializedName("new_password")
    val newPassword: String
)

