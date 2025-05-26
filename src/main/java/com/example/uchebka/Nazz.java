package com.example.uchebka;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
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

public class Nazz extends AppCompatActivity {

    private static final String SUPABASE_URL_user = "https://bvldcmhvkifayhlxjmpz.supabase.co/rest/v1/users";
    private static final String SUPABASE_API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJ2bGRjbWh2a2lmYXlobHhqbXB6Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDc4OTcxNzQsImV4cCI6MjA2MzQ3MzE3NH0.kJMQw8rAfl48W-jd2CxqPBHLBcT9ePI9wCdTImbyTrE";

    private Spinner roleSpinner;
    private EditText userLoginEditText;
    private Button assignButton, backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nazz);

        roleSpinner = findViewById(R.id.roleSpinner);
        userLoginEditText = findViewById(R.id.UserLogin);
        assignButton = findViewById(R.id.Assign);
        backButton = findViewById(R.id.Back);

        String[] roles = {"Студент", "Преподаватель", "Администратор"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, roles);
        roleSpinner.setAdapter(adapter);

        assignButton.setOnClickListener(v -> {
            String role = roleSpinner.getSelectedItem().toString();
            String login = userLoginEditText.getText().toString().trim();

            if (login.isEmpty()) {
                Toast.makeText(this, "Введите логин пользователя", Toast.LENGTH_SHORT).show();
            } else {
                updateUserRole(login, role);
            }
        });

        backButton.setOnClickListener(v -> {
            startActivity(new Intent(this, AdminHome.class));
            finish();
        });
    }

    private void updateUserRole(String login, String newRole) {
        findUserByLogin(login, userId -> {
            if (userId == null) {
                runOnUiThread(() ->
                        Toast.makeText(Nazz.this, "Пользователь не найден", Toast.LENGTH_SHORT).show());
                return;
            }

            updateRoleInDatabase(userId, newRole);
        });
    }

    private void findUserByLogin(String login, UserIdCallback callback) {
        OkHttpClient client = new OkHttpClient();

        String url = SUPABASE_URL_user + "?select=id&login=eq." + login;

        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("apikey", SUPABASE_API_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_API_KEY)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("SupabaseRequest", "Ошибка поиска пользователя", e);
                callback.onResult(null);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    if (response.isSuccessful()) {
                        String responseBody = response.body().string();
                        if (responseBody != null && !responseBody.equals("[]")) {
                            JSONObject user = new JSONArray(responseBody).getJSONObject(0);
                            String userId = user.getString("id");
                            callback.onResult(userId);
                        } else {
                            callback.onResult(null);
                        }
                    } else {
                        Log.e("SupabaseRequest", "Ошибка поиска: " + response.code());
                        callback.onResult(null);
                    }
                } catch (JSONException e) {
                    Log.e("SupabaseRequest", "Ошибка парсинга JSON", e);
                    callback.onResult(null);
                } finally {
                    response.close();
                }
            }
        });
    }

    private void updateRoleInDatabase(String userId, String newRole) {
        JSONObject updateData = new JSONObject();
        try {
            updateData.put("role", newRole);
        } catch (JSONException e) {
            e.printStackTrace();
            runOnUiThread(() ->
                    Toast.makeText(this, "Ошибка формирования данных", Toast.LENGTH_SHORT).show());
            return;
        }

        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(
                updateData.toString(),
                MediaType.get("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url(SUPABASE_URL_user + "?id=eq." + userId)
                .patch(body)
                .addHeader("apikey", SUPABASE_API_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_API_KEY)
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "return=minimal")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(Nazz.this, "Ошибка соединения", Toast.LENGTH_SHORT).show();
                    Log.e("SupabaseRequest", "Ошибка обновления роли", e);
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    if (response.isSuccessful()) {
                        runOnUiThread(() -> {
                            Toast.makeText(Nazz.this,
                                    "Роль успешно обновлена",
                                    Toast.LENGTH_LONG).show();
                            userLoginEditText.setText("");
                        });
                    } else {
                        String errorBody = response.body() != null ? response.body().string() : "empty body";
                        Log.e("SupabaseRequest",
                                "Ошибка обновления: " + response.code() + " - " + errorBody);

                        runOnUiThread(() -> {
                            String errorMsg = "Ошибка обновления роли (" + response.code() + ")";
                            Toast.makeText(Nazz.this, errorMsg, Toast.LENGTH_LONG).show();
                        });
                    }
                } catch (Exception e) {
                    Log.e("SupabaseRequest", "Ошибка обработки ответа", e);
                } finally {
                    response.close();
                }
            }
        });
    }
    interface UserIdCallback {
        void onResult(String userId);
    }
}