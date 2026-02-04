package com.campusaula.edbole.kanban_clone_android.network

import com.campusaula.edbole.kanban_clone_android.kanban.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @GET("/ping")
    suspend fun ping(): Response<Unit>

    @POST("auth/login/")
    suspend fun login(@Body userLogin: UserLogin): Response<LoginResponse>

    @POST("me/logout/")
    suspend fun logout(): Response<Unit>

    @DELETE("me/delete-me/")
    suspend fun deleteMe(): Response<Unit>

    @GET("me/")
    suspend fun getMe(): Response<ProjectUser>

    @GET("users/{user_id}/")
    suspend fun getUserById(@Path("user_id") userId: Int): Response<UserBase>

    @GET("users/{user_id}/projects/")
    suspend fun getUserProjectsByUserId(@Path("user_id") userId: Int): Response<List<ProjectBase>>

    @POST("users/")
    suspend fun createUser(@Body userLogin: UserCreate): Response<UserBase>

    // Projects endpoints

    @GET("projects/")
    suspend fun getAllProjects(): Response<List<Project>>

    @GET("projects/{project_id}/")
    suspend fun getProjectById(@Path("project_id") projectId: Int): Response<Project>

    @GET("projects/{project_id}/users/")
    suspend fun getProjectUsers(@Path("project_id") projectId: Int): Response<List<UserBase>>

    @POST("projects/")
    suspend fun createProject(@Body projectCreate: ProjectCreate): Response<ProjectBase>

    @PUT("projects/{project_id}/")
    suspend fun updateProject(@Path("project_id") projectId: Int, @Body projectCreate: ProjectCreate): Response<ProjectBase>

    @DELETE("projects/{project_id}/")
    suspend fun deleteProject(@Path("project_id") projectId: Int): Response<Unit>

    // Tasks endpoints

    @GET("projects/{project_id}/tasks/")
    suspend fun getProjectTasks(@Path("project_id") projectId: Int): Response<List<Task>>

    @POST("projects/{project_id}/tasks/")
    suspend fun createTask(@Path("project_id") projectId: Int, @Body taskBase: TaskBase): Response<TaskBase>

}
