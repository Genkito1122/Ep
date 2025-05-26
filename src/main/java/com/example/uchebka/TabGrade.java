package com.example.uchebka;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
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
import okhttp3.Response;

public class TabGrade extends AppCompatActivity {

    private static final String SUPABASE_URLGrade = "https://bvldcmhvkifayhlxjmpz.supabase.co/rest/v1/grades";
    private static final String SUPABASE_API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJ2bGRjbWh2a2lmYXlobHhqbXB6Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDc4OTcxNzQsImV4cCI6MjA2MzQ3MzE3NH0.kJMQw8rAfl48W-jd2CxqPBHLBcT9ePI9wCdTImbyTrE";

    private TableLayout tableLayout;
    private Button btnBack, btnRefresh;
    private OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tabgrade);

        tableLayout = findViewById(R.id.tableLayout);
        btnBack = findViewById(R.id.btnBack);
        btnRefresh = findViewById(R.id.btnRefresh);

        btnBack.setOnClickListener(v -> {
            startActivity(new Intent(this, Grade.class));
            finish();
        });
        btnRefresh.setOnClickListener(v -> loadGrades());

        loadGrades();
    }

    private void loadGrades() {
        String url = SUPABASE_URLGrade + "?select=userid,courseid,value,id";
        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("apikey", SUPABASE_API_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_API_KEY)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(TabGrade.this,
                                "Ошибка загрузки оценок", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    if (response.isSuccessful()) {
                        JSONArray gradesArray = new JSONArray(response.body().string());
                        runOnUiThread(() -> updateGradesTable(gradesArray));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void updateGradesTable(JSONArray gradesArray) {
        tableLayout.removeViews(1, tableLayout.getChildCount() - 1);

        try {
            for (int i = 0; i < gradesArray.length(); i++) {
                JSONObject grade = gradesArray.getJSONObject(i);
                String id = grade.getString("id");
                String userId = grade.getString("userid");
                String courseId = grade.getString("courseid");
                String value = grade.optString("value", "0");

                addGradeRow(id, userId, courseId, value);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void addGradeRow(final String id, String userId, String courseId, String value) {
        TableRow row = new TableRow(this);

        TextView tvUserId = createTextView(userId);
        TextView tvCourseId = createTextView(courseId);
        TextView tvValue = createTextView(value);

        Button btnDelete = new Button(this);
        btnDelete.setText("Удалить");
        btnDelete.setOnClickListener(v -> deleteGrade(id));

        row.addView(tvUserId);
        row.addView(tvCourseId);
        row.addView(tvValue);
        row.addView(btnDelete);

        tableLayout.addView(row);
    }

    private void deleteGrade(String id) {
        new AlertDialog.Builder(this)
                .setTitle("Подтверждение")
                .setMessage("Удалить эту оценку?")
                .setPositiveButton("Да", (dialog, which) -> {
                    Request request = new Request.Builder()
                            .url(SUPABASE_URLGrade + "?id=eq." + id)
                            .delete()
                            .addHeader("apikey", SUPABASE_API_KEY)
                            .addHeader("Authorization", "Bearer " + SUPABASE_API_KEY)
                            .build();

                    client.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            runOnUiThread(() ->
                                    Toast.makeText(TabGrade.this,
                                            "Ошибка удаления", Toast.LENGTH_SHORT).show());
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            runOnUiThread(() -> {
                                if (response.isSuccessful()) {
                                    Toast.makeText(TabGrade.this,
                                            "Оценка удалена", Toast.LENGTH_SHORT).show();
                                    loadGrades();
                                }
                            });
                        }
                    });
                })
                .setNegativeButton("Нет", null)
                .show();
    }

    private TextView createTextView(String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setPadding(16, 16, 16, 16);
        tv.setTextSize(14);
        return tv;
    }
}