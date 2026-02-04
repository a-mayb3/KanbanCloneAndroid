package com.campusaula.edbole.kanban_clone_android.kanban

data class LoginResponse(
    val message: String?,
    val user: LoginUser?
)

data class LoginUser(
    val id: String?,
    val name: String?,
    val email: String?
)

// Error response from the API (e.g. 401 Unauthorized)
data class ErrorResponse(
    val detail: String?
)
