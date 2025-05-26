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

public class Grade extends AppCompatActivity {

    private static final String SUPABASE_URLUser = "https://bvldcmhvkifayhlxjmpz.supabase.co/rest/v1/users";
    private static final String SUPABASE_URLCours = "https://bvldcmhvkifayhlxjmpz.supabase.co/rest/v1/courses";
    private static final String SUPABASE_URLGrade = "https://bvldcmhvkifayhlxjmpz.supabase.co/rest/v1/grades";
    private static final String SUPABASE_API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJ2bGRjbWh2a2lmYXlobHhqbXB6Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDc4OTcxNzQsImV4cCI6MjA2MzQ3MzE3NH0.kJMQw8rAfl48W-jd2CxqPBHLBcT9ePI9wCdTImbyTrE";

    private Spinner spinnerCourse;
    private EditText etGrade, etStudentLogin;
    private Button btnAddGrade, btnBack, btnViewGrades;

    private List<String> courseIds = new ArrayList<>();
    private List<String> courseNames = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.grade);

        initViews();
        loadCourses();
        btnAddGrade.setOnClickListener(v -> addGrade());
        btnBack.setOnClickListener(v -> {
            startActivity(new Intent(this, TeacherHome.class));
            finish();
        });
        btnViewGrades.setOnClickListener(v -> {
            startActivity(new Intent(this, TabGrade.class));
            finish();
        });
    }

    private void initViews() {
        etStudentLogin = findViewById(R.id.etStudentLogin);
        spinnerCourse = findViewById(R.id.spinnerCourse);
        etGrade = findViewById(R.id.etGrade);
        btnAddGrade = findViewById(R.id.btnAddGrade);
        btnBack = findViewById(R.id.btnBack);
        btnViewGrades = findViewById(R.id.btnViewGrades);
    }

    private void loadCourses() {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(SUPABASE_URLCours + "?select=id,name")
                .get()
                .addHeader("apikey", SUPABASE_API_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_API_KEY)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(Grade.this, "Ошибка загрузки курсов", Toast.LENGTH_SHORT).show();
                    Log.e("Supabase", "Ошибка загрузки курсов", e);
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    if (response.isSuccessful()) {
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
                                    Grade.this,
                                    android.R.layout.simple_spinner_dropdown_item,
                                    courseNames);
                            spinnerCourse.setAdapter(adapter);
                        });
                    }
                } catch (Exception e) {
                    Log.e("Supabase", "Ошибка обработки данных", e);
                } finally {
                    response.close();
                }
            }
        });
    }

    private void addGrade() {
        String studentLogin = etStudentLogin.getText().toString().trim();
        int coursePos = spinnerCourse.getSelectedItemPosition();
        String gradeStr = etGrade.getText().toString().trim();

        if (studentLogin.isEmpty()) {
            Toast.makeText(this, "Введите логин студента", Toast.LENGTH_SHORT).show();
            return;
        }

        if (coursePos < 0) {
            Toast.makeText(this, "Выберите курс", Toast.LENGTH_SHORT).show();
            return;
        }

        if (gradeStr.isEmpty()) {
            Toast.makeText(this, "Введите оценку", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            int gradeValue = Integer.parseInt(gradeStr);
            if (gradeValue < 0 || gradeValue > 100) {
                Toast.makeText(this, "Оценка должна быть от 0 до 100", Toast.LENGTH_SHORT).show();
                return;
            }

            String courseId = courseIds.get(coursePos);
            String courseName = courseNames.get(coursePos);

            findStudentId(studentLogin, studentId -> {
                if (studentId == null) {
                    runOnUiThread(() ->
                            Toast.makeText(Grade.this, "Студент с таким логином не найден", Toast.LENGTH_LONG).show());
                    return;
                }

                saveGradeToSupabase(studentId, courseId, gradeValue, studentLogin, courseName);
            });
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Некорректная оценка", Toast.LENGTH_SHORT).show();
        }
    }

    private void findStudentId(String login, StudentIdCallback callback) {
        OkHttpClient client = new OkHttpClient();

        String url = SUPABASE_URLUser + "?select=id&login=eq." + login + "&role=eq.Студент";

        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("apikey", SUPABASE_API_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_API_KEY)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("Supabase", "Ошибка поиска студента", e);
                runOnUiThread(() ->
                        Toast.makeText(Grade.this, "Ошибка соединения", Toast.LENGTH_SHORT).show());
                callback.onResult(null);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    if (response.isSuccessful()) {
                        String responseBody = response.body().string();
                        if (responseBody != null && !responseBody.equals("[]")) {
                            JSONObject student = new JSONArray(responseBody).getJSONObject(0);
                            callback.onResult(student.getString("id"));
                        } else {
                            callback.onResult(null);
                        }
                    }
                } catch (Exception e) {
                    Log.e("Supabase", "Ошибка обработки данных", e);
                    callback.onResult(null);
                } finally {
                    response.close();
                }
            }
        });
    }

    private void saveGradeToSupabase(String studentId, String courseId, int gradeValue,
                                     String studentLogin, String courseName) {
        JSONObject gradeData = new JSONObject();
        try {
            gradeData.put("userid", studentId);
            gradeData.put("courseid", courseId);
            gradeData.put("value", gradeValue);
        } catch (JSONException e) {
            e.printStackTrace();
            runOnUiThread(() ->
                    Toast.makeText(this, "Ошибка создания данных", Toast.LENGTH_SHORT).show());
            return;
        }

        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(
                gradeData.toString(),
                MediaType.get("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url(SUPABASE_URLGrade)
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
                    Toast.makeText(Grade.this, "Ошибка соединения", Toast.LENGTH_SHORT).show();
                    Log.e("Supabase", "Ошибка сохранения оценки", e);
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    if (response.isSuccessful()) {
                        runOnUiThread(() -> {
                            String message = String.format("Оценка %d за курс '%s' студенту %s добавлена", gradeValue, courseName, studentLogin);
                            Toast.makeText(Grade.this, message, Toast.LENGTH_LONG).show();
                            etGrade.setText("");
                        });
                    } else {
                        String errorBody = response.body() != null ? response.body().string() : "empty body";
                        int statusCode = response.code();


                        Log.e("Supabase", "Ошибка сохранения: " + statusCode + " - " + errorBody);


                        runOnUiThread(() -> {
                            Toast.makeText(Grade.this,
                                    "Ошибка сохранения (" + statusCode + "): " + errorBody,
                                    Toast.LENGTH_LONG).show();
                        });
                    }
                } finally {
                    response.close();
                }
            }
        });
    }

    interface StudentIdCallback {
        void onResult(String studentId);
    }
}