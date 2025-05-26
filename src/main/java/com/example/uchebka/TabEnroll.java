package com.example.uchebka;

import android.content.Intent;
import android.os.Bundle;
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
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class TabEnroll extends AppCompatActivity {

    private static final String SUPABASE_URLEnroll = "https://bvldcmhvkifayhlxjmpz.supabase.co/rest/v1/enrollments";
    private static final String SUPABASE_API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJ2bGRjbWh2a2lmYXlobHhqbXB6Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDc4OTcxNzQsImV4cCI6MjA2MzQ3MzE3NH0.kJMQw8rAfl48W-jd2CxqPBHLBcT9ePI9wCdTImbyTrE";

    private TableLayout tableLayout;
    private Button btnBack, btnRefresh;
    private OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tabenroll);

        tableLayout = findViewById(R.id.tableLayout);
        btnBack = findViewById(R.id.btnBack);
        btnRefresh = findViewById(R.id.btnRefresh);

        btnBack.setOnClickListener(v -> {
            startActivity(new Intent(this, TeacherHome.class));
            finish();
        });
        btnRefresh.setOnClickListener(v -> loadEnrollments());

        loadEnrollments();
    }

    private void loadEnrollments() {
        String url = SUPABASE_URLEnroll + "?select=id,name,courseid";
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
                        Toast.makeText(TabEnroll.this,
                                "Ошибка загрузки записей", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    if (response.isSuccessful()) {
                        JSONArray enrollmentsArray = new JSONArray(response.body().string());
                        runOnUiThread(() -> updateEnrollmentsTable(enrollmentsArray));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void updateEnrollmentsTable(JSONArray enrollmentsArray) {
        tableLayout.removeViews(1, tableLayout.getChildCount() - 1);

        try {
            for (int i = 0; i < enrollmentsArray.length(); i++) {
                JSONObject enrollment = enrollmentsArray.getJSONObject(i);
                String id = enrollment.getString("id");
                String name = enrollment.getString("name");
                String courseId = enrollment.getString("courseid");

                addEnrollmentRow(id, name, courseId);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void addEnrollmentRow(final String id, String name, String courseId) {
        TableRow row = new TableRow(this);

        TextView tvName = createTextView(name);
        TextView tvCourseId = createTextView(courseId);

        Button btnDelete = new Button(this);
        btnDelete.setText("Удалить");
        btnDelete.setOnClickListener(v -> deleteEnrollment(id));

        row.addView(tvName);
        row.addView(tvCourseId);
        row.addView(btnDelete);

        tableLayout.addView(row);
    }

    private void deleteEnrollment(String id) {
        new AlertDialog.Builder(this)
                .setTitle("Подтверждение")
                .setMessage("Удалить эту запись?")
                .setPositiveButton("Да", (dialog, which) -> {
                    Request request = new Request.Builder()
                            .url(SUPABASE_URLEnroll + "?id=eq." + id)
                            .delete()
                            .addHeader("apikey", SUPABASE_API_KEY)
                            .addHeader("Authorization", "Bearer " + SUPABASE_API_KEY)
                            .build();

                    client.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            runOnUiThread(() ->
                                    Toast.makeText(TabEnroll.this,
                                            "Ошибка удаления", Toast.LENGTH_SHORT).show());
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            runOnUiThread(() -> {
                                if (response.isSuccessful()) {
                                    Toast.makeText(TabEnroll.this,
                                            "Запись удалена", Toast.LENGTH_SHORT).show();
                                    loadEnrollments();
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
