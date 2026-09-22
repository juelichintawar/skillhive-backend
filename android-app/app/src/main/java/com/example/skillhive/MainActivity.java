package com.example.skillhive;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.skillhive.model.LoginRequest;
import com.example.skillhive.model.LoginResponse;
import com.example.skillhive.network.AuthApi;
import com.example.skillhive.network.RetrofitClient;
import com.example.skillhive.utils.SessionManager;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private TextInputEditText etEmail;
    private TextInputEditText etPassword;
    private Button btnLogin;
    private TextView tvRegister;
    private ProgressBar progressBar;

    private AuthApi authApi;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main),
                (v, insets) -> {

                    Insets systemBars = insets.getInsets(
                            WindowInsetsCompat.Type.systemBars()
                    );

                    v.setPadding(
                            systemBars.left,
                            systemBars.top,
                            systemBars.right,
                            systemBars.bottom
                    );

                    return insets;
                }
        );

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);
        progressBar = findViewById(R.id.progressBar);

        authApi = RetrofitClient
                .getInstance()
                .create(AuthApi.class);

        sessionManager = new SessionManager(this);
        if (sessionManager.isLoggedIn()) {

            Intent intent = new Intent(
                    MainActivity.this,
                    HomeActivity.class
            );

            intent.putExtra(
                    "fullName",
                    sessionManager.getFullName()
            );

            startActivity(intent);

            finish();

            return;
        }

        btnLogin.setOnClickListener(v -> loginUser());
        tvRegister.setOnClickListener(v -> {

            Intent intent = new Intent(
                    MainActivity.this,
                    RegisterActivity.class
            );

            startActivity(intent);
        });
    }

    private void loginUser() {

        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty()) {
            etEmail.setError("Enter your email");
            etEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            etPassword.setError("Enter your password");
            etPassword.requestFocus();
            return;
        }

        LoginRequest request = new LoginRequest(email, password);

        setLoading(true);

        authApi.login(request).enqueue(new Callback<LoginResponse>() {

            @Override
            public void onResponse(
                    Call<LoginResponse> call,
                    Response<LoginResponse> response) {

                setLoading(false);

                if (response.isSuccessful() && response.body() != null) {

                    LoginResponse loginResponse = response.body();

                    sessionManager.saveSession(
                            loginResponse.getToken(),
                            loginResponse.getId(),
                            loginResponse.getFullName(),
                            loginResponse.getEmail(),
                            loginResponse.getRole()
                    );

                    Toast.makeText(
                            MainActivity.this,
                            "Login successful",
                            Toast.LENGTH_SHORT
                    ).show();

                    Intent intent = new Intent(
                            MainActivity.this,
                            HomeActivity.class
                    );

                    intent.putExtra(
                            "fullName",
                            loginResponse.getFullName()
                    );

                    startActivity(intent);

                    finish();

                } else {

                    Toast.makeText(
                            MainActivity.this,
                            "Invalid email or password",
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }

            @Override
            public void onFailure(
                    Call<LoginResponse> call,
                    Throwable t) {

                setLoading(false);

                Toast.makeText(
                        MainActivity.this,
                        "Connection failed: " + t.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    private void setLoading(boolean loading) {

        if (loading) {
            progressBar.setVisibility(View.VISIBLE);
            btnLogin.setEnabled(false);
        } else {
            progressBar.setVisibility(View.GONE);
            btnLogin.setEnabled(true);
        }
    }
}