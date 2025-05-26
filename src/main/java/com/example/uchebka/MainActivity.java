package com.example.uchebka;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private static final String SUPABASE_URL_users = "https://bvldcmhvkifayhlxjmpz.supabase.co/rest/v1/users";
    private static final String SUPABASE_API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJ2bGRjbWh2a2lmYXlobHhqbXB6Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDc4OTcxNzQsImV4cCI6MjA2MzQ3MzE3NH0.kJMQw8rAfl48W-jd2CxqPBHLBcT9ePI9wCdTImbyTrE";
    EditText etLogin, etPassword;
    Button btnLogin;
    TextView tvToRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etLogin = findViewById(R.id.etLogin);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvToRegister = findViewById(R.id.tvToRegister);

        btnLogin.setOnClickListener(v -> {
            String login = etLogin.getText().toString().trim();
            String password = etPassword.getText().toString();

            if (login.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Введите логин и пароль!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (login.isEmpty()) {
                Toast.makeText(this, "Введите логин!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (password.isEmpty()) {
                Toast.makeText(this, "Введите пароль!", Toast.LENGTH_SHORT).show();
                return;
            } else {
                authorizeUser(login, password);
            }
        });


        tvToRegister.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, RegisterActivity.class));
            finish();
        });
    }
    private void authorizeUser(String login, String password) {
        OkHttpClient client = new OkHttpClient();

        String filterUrl = SUPABASE_URL_users + "?login=eq." + login + "&select=password,role";
        Request request = new Request.Builder()
                .url(filterUrl)
                .get()
                .addHeader("apikey", SUPABASE_API_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_API_KEY)
                .addHeader("Content-Type", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(MainActivity.this, "Ошибка подключения", Toast.LENGTH_SHORT).show()
                );
                Log.e("Auth", "Ошибка подключения", e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    runOnUiThread(() ->
                            Toast.makeText(MainActivity.this, "Ошибка авторизации", Toast.LENGTH_SHORT).show()
                    );
                    return;
                }

                String jsonResponse = response.body().string();
                try {
                    JSONArray jsonArray = new JSONArray(jsonResponse);
                    if (jsonArray.length() == 0) {
                        runOnUiThread(() ->
                                Toast.makeText(MainActivity.this, "Пользователь не найден", Toast.LENGTH_SHORT).show()
                        );
                        return;
                    }

                    JSONObject user = jsonArray.getJSONObject(0);
                    String storedPassword = user.getString("password");
                    String role = user.optString("role", "");

                    if (storedPassword.equals(password)) {
                        runOnUiThread(() -> {
                            Toast.makeText(MainActivity.this, "Вход выполнен!", Toast.LENGTH_SHORT).show();

                            if ("Студент".equalsIgnoreCase(role)) {
                                startActivity(new Intent(MainActivity.this, StudentHome.class));
                            }
                            if ("Администратор".equalsIgnoreCase(role)) {
                                startActivity(new Intent(MainActivity.this, AdminHome.class));
                            }
                            if ("Преподаватель".equalsIgnoreCase(role)) {
                                startActivity(new Intent(MainActivity.this, TeacherHome.class));
                            }
                        });
                    } else {
                        runOnUiThread(() ->
                                Toast.makeText(MainActivity.this, "Неверный пароль", Toast.LENGTH_SHORT).show()
                        );
                    }

                }catch (JSONException e) {
                    Log.e("Auth", "Ошибка разбора JSON", e);
                    runOnUiThread(() ->
                            Toast.makeText(MainActivity.this, "Ошибка данных: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
                }
            }
        });
    }
}
