package com.example.skillhive;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.skillhive.model.RegisterRequest;
import com.example.skillhive.model.RegisterResponse;
import com.example.skillhive.network.AuthApi;
import com.example.skillhive.network.RetrofitClient;

import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText etFullName;
    private TextInputEditText etEmail;
    private TextInputEditText etPassword;
    private TextInputEditText etPhone;
    private TextInputEditText etCollege;

    private Button btnRegister;
    private ProgressBar progressBarRegister;

    private AuthApi authApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_register);

        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etPhone = findViewById(R.id.etPhone);
        etCollege = findViewById(R.id.etCollege);

        btnRegister = findViewById(R.id.btnRegister);
        progressBarRegister = findViewById(R.id.progressBarRegister);

        authApi = RetrofitClient
                .getInstance()
                .create(AuthApi.class);

        btnRegister.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {

        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String college = etCollege.getText().toString().trim();

        if (fullName.isEmpty()) {
            etFullName.setError("Enter your full name");
            etFullName.requestFocus();
            return;
        }

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

        if (phone.isEmpty()) {
            etPhone.setError("Enter your phone number");
            etPhone.requestFocus();
            return;
        }

        if (college.isEmpty()) {
            etCollege.setError("Enter your college");
            etCollege.requestFocus();
            return;
        }

        RegisterRequest request = new RegisterRequest(
                fullName,
                email,
                password,
                phone,
                college
        );

        setLoading(true);

        authApi.register(request).enqueue(
                new Callback<RegisterResponse>() {

                    @Override
                    public void onResponse(
                            Call<RegisterResponse> call,
                            Response<RegisterResponse> response) {

                        setLoading(false);

                        if (response.isSuccessful()
                                && response.body() != null) {

                            Toast.makeText(
                                    RegisterActivity.this,
                                    "Registration successful!",
                                    Toast.LENGTH_LONG
                            ).show();

                            Intent intent = new Intent(
                                    RegisterActivity.this,
                                    MainActivity.class
                            );

                            startActivity(intent);
                            finish();

                        } else {

                            Toast.makeText(
                                    RegisterActivity.this,
                                    "Registration failed: "
                                            + response.code(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<RegisterResponse> call,
                            Throwable t) {

                        setLoading(false);

                        Toast.makeText(
                                RegisterActivity.this,
                                "Connection failed: "
                                        + t.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }

    private void setLoading(boolean loading) {

        if (loading) {
            progressBarRegister.setVisibility(View.VISIBLE);
            btnRegister.setEnabled(false);
        } else {
            progressBarRegister.setVisibility(View.GONE);
            btnRegister.setEnabled(true);
        }
    }
}