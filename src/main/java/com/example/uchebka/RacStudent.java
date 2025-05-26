package com.example.uchebka;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class RacStudent extends AppCompatActivity {

    private static final String SUPABASE_URLLesson = "https://bvldcmhvkifayhlxjmpz.supabase.co/rest/v1/lessons";
    private static final String SUPABASE_API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJ2bGRjbWh2a2lmYXlobHhqbXB6Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDc4OTcxNzQsImV4cCI6MjA2MzQ3MzE3NH0.kJMQw8rAfl48W-jd2CxqPBHLBcT9ePI9wCdTImbyTrE";

    private TableLayout tableLayout;
    private Button btnBack, btnRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.racstudent);

        tableLayout = findViewById(R.id.tableLayout);
        btnBack = findViewById(R.id.btnBack);
        btnRefresh = findViewById(R.id.btnRefresh);

        btnBack.setOnClickListener(v -> {
            startActivity(new Intent(this, StudentHome.class));
            finish();
        });
        btnRefresh.setOnClickListener(v -> loadSchedule());

        loadSchedule();
    }

    private void loadSchedule() {
        OkHttpClient client = new OkHttpClient();

        String url = SUPABASE_URLLesson + "?select=date,time,description,courses(name)";

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
                    Toast.makeText(RacStudent.this,
                            "Ошибка загрузки расписания",
                            Toast.LENGTH_SHORT).show();
                    Log.e("ViewSchedule", "Ошибка загрузки", e);
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    if (response.isSuccessful()) {
                        String responseBody = response.body().string();
                        JSONArray scheduleArray = new JSONArray(responseBody);

                        runOnUiThread(() -> {
                            updateScheduleTable(scheduleArray);
                        });
                    } else {
                        Log.e("ViewSchedule", "Ошибка сервера: " + response.code());
                    }
                } catch (JSONException e) {
                    Log.e("ViewSchedule", "Ошибка парсинга", e);
                } finally {
                    response.close();
                }
            }
        });
    }

    private void updateScheduleTable(JSONArray scheduleArray) {
        tableLayout.removeViews(1, tableLayout.getChildCount() - 1);

        try {
            // Добавляем строки с данными
            for (int i = 0; i < scheduleArray.length(); i++) {
                JSONObject lesson = scheduleArray.getJSONObject(i);
                JSONObject course = lesson.getJSONObject("courses");

                String date = lesson.getString("date");
                String time = lesson.getString("time");
                String courseName = course.getString("name");
                String description = lesson.optString("description", "");

                addTableRow(date, time, courseName, description);
            }
        } catch (JSONException e) {
            Log.e("ViewSchedule", "Ошибка обработки данных", e);
        }
    }

    private void addTableRow(String date, String time, String course, String description) {
        TableRow row = new TableRow(this);

        TextView tvDate = createTextView(date);
        TextView tvTime = createTextView(time);
        TextView tvCourse = createTextView(course);
        TextView tvDesc = createTextView(description);

        row.addView(tvDate);
        row.addView(tvTime);
        row.addView(tvCourse);
        row.addView(tvDesc);

        tableLayout.addView(row);
    }

    private TextView createTextView(String text) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setPadding(16, 16, 16, 16);
        textView.setTextSize(14);
        return textView;
    }
}
