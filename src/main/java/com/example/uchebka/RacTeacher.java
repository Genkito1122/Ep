package com.example.uchebka;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RacTeacher extends AppCompatActivity {

    private static final String SUPABASE_URL_COURSES = "https://bvldcmhvkifayhlxjmpz.supabase.co/rest/v1/courses";
    private static final String SUPABASE_URL_LESSONS = "https://bvldcmhvkifayhlxjmpz.supabase.co/rest/v1/lessons";
    private static final String SUPABASE_API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJ2bGRjbWh2a2lmYXlobHhqbXB6Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDc4OTcxNzQsImV4cCI6MjA2MzQ3MzE3NH0.kJMQw8rAfl48W-jd2CxqPBHLBcT9ePI9wCdTImbyTrE";

    private Spinner courseSpinner;
    private TextView etDate, etTime;
    private EditText etDescription;
    private Button btnAddSchedule, btnViewSchedule, btnBack;
    private List<String> courseids = new ArrayList<>();
    private List<String> coursenames = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.racteacher);

        initViews();
        loadCourses();
        setupListeners();
    }

    private void initViews() {
        courseSpinner = findViewById(R.id.courseSpinner);
        etDate = findViewById(R.id.etDate);
        etTime = findViewById(R.id.etTime);
        etDescription = findViewById(R.id.etDescription);
        btnAddSchedule = findViewById(R.id.btnAddSchedule);
        btnViewSchedule = findViewById(R.id.btnViewSchedule);
        btnBack = findViewById(R.id.btnBack);
    }

    private void loadCourses() {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(SUPABASE_URL_COURSES + "?select=id,name")
                .get()
                .addHeader("apikey", SUPABASE_API_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_API_KEY)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(RacTeacher.this, "Ошибка загрузки курсов", Toast.LENGTH_SHORT).show();
                    Log.e("RacTeacher", "Ошибка загрузки курсов", e);
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    if (response.isSuccessful()) {
                        String responseBody = response.body().string();
                        JSONArray coursesArray = new JSONArray(responseBody);

                        courseids.clear();
                        coursenames.clear();

                        for (int i = 0; i < coursesArray.length(); i++) {
                            JSONObject course = coursesArray.getJSONObject(i);
                            courseids.add(course.getString("id"));
                            coursenames.add(course.getString("name"));
                        }

                        runOnUiThread(() -> {
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                    RacTeacher.this,
                                    android.R.layout.simple_spinner_dropdown_item,
                                    coursenames);
                            courseSpinner.setAdapter(adapter);
                        });
                    }
                } catch (Exception e) {
                    Log.e("RacTeacher", "Ошибка обработки данных", e);
                } finally {
                    response.close();
                }
            }
        });
    }

    private void setupListeners() {
        btnAddSchedule.setOnClickListener(v -> addSchedule());
        btnBack.setOnClickListener(v -> {
            startActivity(new Intent(this, TeacherHome.class));
            finish();
        });
        btnViewSchedule.setOnClickListener(v -> {
            startActivity(new Intent(this, TableRac.class));
            finish();
        });

        etDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    RacTeacher.this,
                    (view, year, month, dayOfMonth) -> {
                        String formattedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
                        etDate.setText(formattedDate);
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show();
        });

        etTime.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            TimePickerDialog timePickerDialog = new TimePickerDialog(
                    RacTeacher.this,
                    (view, hourOfDay, minute) -> {
                        String formattedTime = String.format(Locale.getDefault(), "%02d:%02d:00", hourOfDay, minute);
                        etTime.setText(formattedTime);
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true
            );
            timePickerDialog.show();
        });
    }

    private void addSchedule() {
        int coursePos = courseSpinner.getSelectedItemPosition();
        String date = etDate.getText().toString().trim();
        String time = etTime.getText().toString().trim();
        String description = etDescription.getText().toString().trim();

        if (coursePos < 0) {
            Toast.makeText(this, "Выберите курс", Toast.LENGTH_SHORT).show();
            return;
        }
        if (date.isEmpty() || time.isEmpty()) {
            Toast.makeText(this, "Заполните дату и время", Toast.LENGTH_SHORT).show();
            return;
        }
        if (date.isEmpty()) {
            Toast.makeText(this, "Заполните дату", Toast.LENGTH_SHORT).show();
            return;
        }
                if (time.isEmpty()) {
            Toast.makeText(this, "Заполните время", Toast.LENGTH_SHORT).show();
            return;
        }
        

        String courseId = courseids.get(coursePos);
        String title = coursenames.get(coursePos);

        saveScheduleToSupabase(courseId, date, time, description, title);
    }

    private void saveScheduleToSupabase(String courseId, String date, String time,
                                        String description, String title) {
        try {
            JSONObject scheduleData = new JSONObject();
            scheduleData.put("title", title);
            scheduleData.put("date", date);
            scheduleData.put("time", time);
            scheduleData.put("description", description);
            scheduleData.put("courseid", courseId);

            Log.d("RacTeacher", "JSON к отправке: " + scheduleData.toString());

            OkHttpClient client = new OkHttpClient();
            RequestBody body = RequestBody.create(
                    scheduleData.toString(),
                    MediaType.get("application/json; charset=utf-8")
            );

            Request request = new Request.Builder()
                    .url(SUPABASE_URL_LESSONS)
                    .post(body)
                    .addHeader("apikey", SUPABASE_API_KEY)
                    .addHeader("Authorization", "Bearer " + SUPABASE_API_KEY)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Prefer", "return=representation")
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> {
                        Toast.makeText(RacTeacher.this, "Ошибка соединения", Toast.LENGTH_SHORT).show();
                        Log.e("RacTeacher", "Ошибка соединения", e);
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        String responseBody = response.body() != null ? response.body().string() : "empty";
                        if (response.isSuccessful()) {
                            runOnUiThread(() -> {
                                Toast.makeText(RacTeacher.this,
                                        "Расписание успешно добавлено",
                                        Toast.LENGTH_LONG).show();
                                clearForm();
                            });
                        } else {
                            Log.e("RacTeacher", "Ошибка сохранения: " + response.code() + "\n" + responseBody);
                            runOnUiThread(() -> Toast.makeText(RacTeacher.this,
                                    "Ошибка: " + response.code() + "\n" + responseBody,
                                    Toast.LENGTH_LONG).show());
                        }
                    } finally {
                        response.close();
                    }
                }
            });
        } catch (JSONException e) {
            Log.e("RacTeacher", "Ошибка JSON: " + e.getMessage());
            runOnUiThread(() -> Toast.makeText(this, "Ошибка данных", Toast.LENGTH_LONG).show());
        }
    }

    private void clearForm() {
        etDate.setText("");
        etTime.setText("");
        etDescription.setText("");
    }
}
