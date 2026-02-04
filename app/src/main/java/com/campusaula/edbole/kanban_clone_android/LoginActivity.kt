package com.campusaula.edbole.kanban_clone_android

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.campusaula.edbole.kanban_clone_android.network.ApiService
import com.campusaula.edbole.kanban_clone_android.network.RetrofitInstance
import kotlinx.coroutines.launch
import retrofit2.Retrofit

class LoginActivity : AppCompatActivity() {

    private lateinit var emailInput : androidx.appcompat.widget.AppCompatEditText
    private lateinit var passwordInput : androidx.appcompat.widget.AppCompatEditText

    private lateinit var loginButton : androidx.appcompat.widget.AppCompatButton
    private lateinit var logonButton : androidx.appcompat.widget.AppCompatButton

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
                android.widget.Toast.makeText(this, "Please enter email and password", android.widget.Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch{
                try {
                    val loginResponse = api.login(
                        com.campusaula.edbole.kanban_clone_android.kanban.UserLogin(
                            email = email,
                            password = password
                        )
                    )

                    if (loginResponse.isSuccessful) {
                        // Después del login exitoso OkHttp/CookieJar habrá guardado las cookies.
                        val baseUrl = retrofit.baseUrl().toString()
                        val authValue = RetrofitInstance.getAuthCookieForUrl(baseUrl)
                        if (authValue != null) {
                            android.widget.Toast.makeText(this@LoginActivity, "Auth cookie guardada", android.widget.Toast.LENGTH_SHORT).show()
                        } else {
                            android.widget.Toast.makeText(this@LoginActivity, "Login OK pero no se encontró cookie de auth", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        android.widget.Toast.makeText(this@LoginActivity, "Login failed: ${loginResponse.code()}", android.widget.Toast.LENGTH_SHORT).show()
                    }

                } catch (ex: Exception){
                    android.widget.Toast.makeText(this@LoginActivity, "Login failed: ${ex.message}", android.widget.Toast.LENGTH_SHORT).show()
                }

            }
        }
    }
}
