package com.example.uchebka;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
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

public class AddCourse extends AppCompatActivity {
    private static final String SUPABASE_URL = "https://bvldcmhvkifayhlxjmpz.supabase.co/rest/v1/courses";
    private static final String SUPABASE_API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJ2bGRjbWh2a2lmYXlobHhqbXB6Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDc4OTcxNzQsImV4cCI6MjA2MzQ3MzE3NH0.kJMQw8rAfl48W-jd2CxqPBHLBcT9ePI9wCdTImbyTrE";

    private EditText etCourseName, etCourseDescription;
    private Button btnAddCourse, btnViewCourses, btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.addcourse);

        initViews();
        setupListeners();
    }

    private void initViews() {
        etCourseName = findViewById(R.id.etCourseName);
        etCourseDescription = findViewById(R.id.etCourseDescription);
        btnAddCourse = findViewById(R.id.btnAddCourse);
        btnViewCourses = findViewById(R.id.btnViewCourses);
        btnBack = findViewById(R.id.btnBack);
    }

    private void setupListeners() {
        btnAddCourse.setOnClickListener(v -> addCourse());
        btnBack.setOnClickListener(v -> {
            startActivity(new Intent(this, TeacherHome.class));
            finish();
        });
        btnViewCourses.setOnClickListener(v -> {
            startActivity(new Intent(this, TabCours.class));
            finish();
        });
    }

    private void addCourse() {
        String name = etCourseName.getText().toString().trim();
        String description = etCourseDescription.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(this, "Введите название курса", Toast.LENGTH_SHORT).show();
            return;
        }

        JSONObject courseData = new JSONObject();
        try {
            courseData.put("name", name);
            courseData.put("description", description);
        } catch (JSONException e) {
            Log.e("AddCourse", "JSON creation error", e);
            Toast.makeText(this, "Ошибка создания данных", Toast.LENGTH_SHORT).show();
            return;
        }

        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(
                courseData.toString(),
                MediaType.get("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url(SUPABASE_URL)
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
                    Toast.makeText(AddCourse.this, "Ошибка соединения", Toast.LENGTH_SHORT).show();
                    Log.e("AddCourse", "Network error", e);
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    if (response.isSuccessful()) {
                        runOnUiThread(() -> {
                            Toast.makeText(AddCourse.this, "Курс успешно добавлен", Toast.LENGTH_LONG).show();
                            clearForm();
                        });
                    } else {
                        String errorBody = response.body() != null ? response.body().string() : "empty body";
                        Log.e("AddCourse", "Error: " + response.code() + " - " + errorBody);

                        runOnUiThread(() -> {
                            String errorMsg = "Ошибка добавления курса";
                            if (response.code() == 409) {
                                errorMsg = "Курс с таким названием уже существует";
                            }
                            Toast.makeText(AddCourse.this, errorMsg, Toast.LENGTH_SHORT).show();
                        });
                    }
                } finally {
                    response.close();
                }
            }
        });
    }
    private void clearForm() {
        etCourseName.setText("");
        etCourseDescription.setText("");
    }
}
