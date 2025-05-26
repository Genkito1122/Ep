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
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class KursStudent extends AppCompatActivity {

    private static final String SUPABASE_URL_user = "https://bvldcmhvkifayhlxjmpz.supabase.co/rest/v1/users";
    private static final String SUPABASE_URL_courses = "https://bvldcmhvkifayhlxjmpz.supabase.co/rest/v1/courses";
    private static final String SUPABASE_URL_enroll = "https://bvldcmhvkifayhlxjmpz.supabase.co/rest/v1/enrollments";
    private static final String SUPABASE_API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJ2bGRjbWh2a2lmYXlobHhqbXB6Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDc4OTcxNzQsImV4cCI6MjA2MzQ3MzE3NH0.kJMQw8rAfl48W-jd2CxqPBHLBcT9ePI9wCdTImbyTrE";

    private Spinner kursSpinner;
    private EditText fioEditText, emailEditText;
    private Button zapisButton, backButton;
    private List<String> courseIds = new ArrayList<>();
    private List<String> courseNames = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.kursstudent);

        kursSpinner = findViewById(R.id.kursSpinner);
        fioEditText = findViewById(R.id.fioEditText);
        emailEditText = findViewById(R.id.emailEditText);
        zapisButton = findViewById(R.id.zapisButton);
        backButton = findViewById(R.id.backButton);

        loadCoursesFromSupabase();

        backButton.setOnClickListener(view -> {
            startActivity(new Intent(this, StudentHome.class));
            finish();
        });
    }

    private void loadCoursesFromSupabase() {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(SUPABASE_URL_courses + "?select=id,name")
                .get()
                .addHeader("apikey", SUPABASE_API_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_API_KEY)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(KursStudent.this, "Ошибка загрузки курсов", Toast.LENGTH_SHORT).show();
                    Log.e("SupabaseRequest", "Ошибка при загрузке курсов", e);
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String responseBody = response.body().string();
                        JSONArray coursesArray = new JSONArray(responseBody);

                        courseIds.clear();
                        courseNames.clear();

                        for (int i = 0; i < coursesArray.length(); i++) {
                            JSONObject course = coursesArray.getJSONObject(i);
                            courseIds.add(course.getString("id"));
                            courseNames.add(course.getString("name"));
                        }

                        runOnUiThread(() -> {
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                    KursStudent.this,
                                    android.R.layout.simple_spinner_dropdown_item,
                                    courseNames);
                            kursSpinner.setAdapter(adapter);

                            setupRegisterButton();
                        });
                    } catch (JSONException e) {
                        Log.e("SupabaseRequest", "Ошибка парсинга курсов", e);
                    }
                } else {
                    Log.e("SupabaseRequest", "Ошибка загрузки курсов: " + response.code());
                }
            }
        });
    }

    private void setupRegisterButton() {
        zapisButton.setOnClickListener(v -> {
            int selectedPosition = kursSpinner.getSelectedItemPosition();
            if (selectedPosition < 0 || selectedPosition >= courseIds.size()) {
                Toast.makeText(this, "Выберите курс", Toast.LENGTH_SHORT).show();
                return;
            }

            String fio = fioEditText.getText().toString().trim();
            String email = emailEditText.getText().toString().trim();

            if (fio.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!email.contains("@") || !email.contains(".")) {
                Toast.makeText(this, "Введите корректный email", Toast.LENGTH_SHORT).show();
                return;
            }

            String courseId = courseIds.get(selectedPosition);
            String courseName = courseNames.get(selectedPosition);

            checkUserAndRegister(fio, email, courseId, courseName);
        });
    }

    private void checkUserAndRegister(String fio, String email, String courseId, String courseName) {
        OkHttpClient client = new OkHttpClient();

        String url = SUPABASE_URL_user + "?select=id&email=eq." + email;

        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("apikey", SUPABASE_API_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_API_KEY)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(KursStudent.this, "Ошибка соединения", Toast.LENGTH_SHORT).show();
                    Log.e("SupabaseRequest", "Ошибка проверки пользователя", e);
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String responseBody = response.body().string();
                        if (responseBody != null && !responseBody.equals("[]")) {
                            JSONArray usersArray = new JSONArray(responseBody);
                            JSONObject user = usersArray.getJSONObject(0);
                            String userId = user.getString("id");

                            createEnrollment(fio, userId, courseId, courseName);
                        } else {
                            runOnUiThread(() -> {
                                Toast.makeText(KursStudent.this,
                                        "Пользователь с таким email не найден",
                                        Toast.LENGTH_SHORT).show();
                            });
                        }
                    } catch (JSONException e) {
                        Log.e("SupabaseRequest", "Ошибка парсинга пользователя", e);
                    }
                } else {
                    Log.e("SupabaseRequest", "Ошибка проверки пользователя: " + response.code());
                }
            }
        });
    }

    private void createEnrollment(String name, String userId, String courseId, String courseName) {
        JSONObject enrollmentData = new JSONObject();
        try {
            enrollmentData.put("name", name);
            enrollmentData.put("userid", userId);
            enrollmentData.put("courseid", courseId);
        } catch (JSONException e) {
            e.printStackTrace();
            runOnUiThread(() ->
                    Toast.makeText(this, "Ошибка создания записи", Toast.LENGTH_SHORT).show());
            return;
        }

        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(
                enrollmentData.toString(),
                MediaType.get("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url(SUPABASE_URL_enroll)
                .post(body)
                .addHeader("apikey", SUPABASE_API_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_API_KEY)
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "return=minimal")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(KursStudent.this, "Ошибка соединения", Toast.LENGTH_SHORT).show();
                    Log.e("SupabaseRequest", "Ошибка записи на курс", e);
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    if (response.isSuccessful()) {
                        runOnUiThread(() -> {
                            Toast.makeText(KursStudent.this,
                                    "Вы успешно записались на курс: " + courseName,
                                    Toast.LENGTH_LONG).show();
                            fioEditText.setText("");
                            emailEditText.setText("");
                        });
                    } else {
                        try {
                            String errorBody = response.body() != null ? response.body().string() : "empty body";
                            Log.e("SupabaseRequest", "Ошибка записи: " + response.code() + " - " + errorBody);
                            runOnUiThread(() -> {
                                Toast.makeText(KursStudent.this,
                                        "Ошибка записи на курс: " + response.code(),
                                        Toast.LENGTH_SHORT).show();
                            });
                        } catch (IOException e) {
                            Log.e("SupabaseRequest", "Ошибка чтения тела ответа", e);
                            runOnUiThread(() -> {
                                Toast.makeText(KursStudent.this,
                                        "Ошибка записи на курс",
                                        Toast.LENGTH_SHORT).show();
                            });
                        }
                    }
                } finally {
                    if (response != null) {
                        response.close();
                    }
                }
            }
        });
    }
}