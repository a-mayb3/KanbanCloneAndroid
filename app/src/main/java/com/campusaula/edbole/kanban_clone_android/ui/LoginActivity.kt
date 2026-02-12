package com.campusaula.edbole.kanban_clone_android.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.campusaula.edbole.kanban_clone_android.R
import com.campusaula.edbole.kanban_clone_android.network.ApiService
import com.campusaula.edbole.kanban_clone_android.network.RetrofitInstance
import com.campusaula.edbole.kanban_clone_android.kanban.ErrorResponse
import com.campusaula.edbole.kanban_clone_android.kanban.UserLogin
import com.google.gson.Gson
import kotlinx.coroutines.launch
import retrofit2.Retrofit

class LoginActivity : AppCompatActivity() {

    private lateinit var emailInput : AppCompatEditText
    private lateinit var passwordInput : AppCompatEditText

    private lateinit var loginButton : AppCompatButton
    private lateinit var logonButton : AppCompatButton

    private lateinit var retrofit : Retrofit
    private lateinit var api : ApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        retrofit = RetrofitInstance.getRetrofit(applicationContext)
        api = retrofit.create(ApiService::class.java)

        emailInput = findViewById(R.id.emailInput)
        passwordInput = findViewById(R.id.passwordInput)
        loginButton = findViewById(R.id.loginButton)
        logonButton = findViewById(R.id.logonButton)

        loginButton.setOnClickListener {
            val email = emailInput.text.toString()
            val password = passwordInput.text.toString()

            if (email.isEmpty() && password.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch{
                try {
                    val loginResponse = api.login(
                        UserLogin(
                            email = email,
                            password = password
                        )
                    )

                    val baseUrl = retrofit.baseUrl().toString()
                    val baseHost = retrofit.baseUrl().host

                    if (loginResponse.isSuccessful) {
                        // Después del login exitoso OkHttp/CookieJar habrá guardado las cookies.
                        val authValue = RetrofitInstance.getAuthCookieForUrl(baseUrl)
                        if (authValue != null) {
                            Toast.makeText(this@LoginActivity, "Auth cookie guardada", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@LoginActivity, "Login OK pero no se encontró cookie de auth", Toast.LENGTH_SHORT).show()
                        }
                        // Navegar a MainActivity
                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        if (loginResponse.code() == 401) {
                            // parse error body if possible
                            val errBody = loginResponse.errorBody()?.string()
                            val gson = Gson()
                            val errMsg = try {
                                val err = gson.fromJson(errBody, ErrorResponse::class.java)
                                err.detail ?: "Unauthorized"
                            } catch (_: Exception) {
                                errBody ?: "Unauthorized"
                            }
                            // clear stored cookies for base host
                            RetrofitInstance.clearCookiesForHost(baseHost)

                            Toast.makeText(this@LoginActivity, "Login failed (401): $errMsg", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@LoginActivity, "Login failed: ${loginResponse.code()}", Toast.LENGTH_SHORT).show()
                        }
                    }

                } catch (ex: Exception){
                    Toast.makeText(this@LoginActivity, "Login failed: ${ex.message}", Toast.LENGTH_SHORT).show()
                }

            }
        }
    }
}
