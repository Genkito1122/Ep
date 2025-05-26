package com.example.uchebka;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RegisterActivity extends AppCompatActivity {

    private static final String SUPABASE_URL_users = "https://bvldcmhvkifayhlxjmpz.supabase.co/rest/v1/users";
    private static final String SUPABASE_API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJ2bGRjbWh2a2lmYXlobHhqbXB6Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDc4OTcxNzQsImV4cCI6MjA2MzQ3MzE3NH0.kJMQw8rAfl48W-jd2CxqPBHLBcT9ePI9wCdTImbyTrE";
    private EditText etNewLogin, etEmail, etNewPassword, etRepeatPassword;
    private Button btnRegister;
    private TextView tvToLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etNewLogin = findViewById(R.id.etNewLogin);
        etEmail = findViewById(R.id.etEmail);
        etNewPassword = findViewById(R.id.etNewPassword);
        etRepeatPassword = findViewById(R.id.etRepeatPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvToLogin = findViewById(R.id.tvToLogin);

        btnRegister.setOnClickListener(v -> {
            String login = etNewLogin.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etNewPassword.getText().toString();
            String repeatPassword = etRepeatPassword.getText().toString();

            if (login.isEmpty() || email.isEmpty() || password.isEmpty() || repeatPassword.isEmpty()) {
                Toast.makeText(this, "Пожалуйста, заполните все поля", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!email.contains("@") || !email.contains(".")) {
                Toast.makeText(this, "Введите корректный email", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!password.equals(repeatPassword)) {
                Toast.makeText(this, "Пароли не совпадают", Toast.LENGTH_SHORT).show();
                return;
            }

            registerUser(login, email, password);
        });

        tvToLogin.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, MainActivity.class));
            finish();
        });
    }
    private void registerUser(String login, String email, String password) {
        JSONObject userData = new JSONObject();
        try {
            userData.put("login", login);
            userData.put("email", email);
            userData.put("password", password);
            userData.put("role", "Студент");
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Ошибка при создании данных", Toast.LENGTH_SHORT).show();
            return;
        }

        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(
                userData.toString(),
                MediaType.get("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url(SUPABASE_URL_users)
                .post(body)
                .addHeader("apikey", SUPABASE_API_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_API_KEY)
                .addHeader("Content-Type", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(RegisterActivity.this, "Ошибка соединения", Toast.LENGTH_SHORT).show();
                    Log.e("SupabaseRequst", "Ошибка при регистрации", e);
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(() -> {
                        Toast.makeText(RegisterActivity.this, "Регистрация успешна.", Toast.LENGTH_LONG).show();
                        startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                        finish();
                    });
                } else {
                    String errorBody = response.body() != null ? response.body().string() : "empty body";
                    Log.e("SupabaseRequst", "Ошибка регистрации: " + response.code() + " - " + response.message() + " | " + errorBody);
                    runOnUiThread(() -> {
                        if (response.code() == 409) {
                            Toast.makeText(RegisterActivity.this, "Пользователь с таким email уже существует", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(RegisterActivity.this, "Ошибка при регистрации", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }
}