package com.example.uchebka;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import okhttp3.RequestBody;
import okhttp3.Response;

public class TabCours extends AppCompatActivity {

    private static final String SUPABASE_URLCours = "https://bvldcmhvkifayhlxjmpz.supabase.co/rest/v1/courses";
    private static final String SUPABASE_API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJ2bGRjbWh2a2lmYXlobHhqbXB6Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDc4OTcxNzQsImV4cCI6MjA2MzQ3MzE3NH0.kJMQw8rAfl48W-jd2CxqPBHLBcT9ePI9wCdTImbyTrE";

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private TableLayout tableLayout;
    private Button btnBack, btnRefresh;
    private OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tabcours);

        tableLayout = findViewById(R.id.tableLayout);
        btnBack = findViewById(R.id.btnBack);
        btnRefresh = findViewById(R.id.btnRefresh);

        btnBack.setOnClickListener(v -> {
            startActivity(new Intent(this, AddCourse.class));
            finish();
        });
        btnRefresh.setOnClickListener(v -> loadCourses());

        loadCourses();
    }

    private void loadCourses() {
        String url = SUPABASE_URLCours + "?select=id,name,description";
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
                        Toast.makeText(TabCours.this,
                                "Ошибка загрузки курсов", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    if (response.isSuccessful()) {
                        JSONArray coursesArray = new JSONArray(response.body().string());
                        runOnUiThread(() -> updateCoursesTable(coursesArray));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void updateCoursesTable(JSONArray coursesArray) {
        tableLayout.removeViews(1, tableLayout.getChildCount() - 1);

        try {
            for (int i = 0; i < coursesArray.length(); i++) {
                JSONObject course = coursesArray.getJSONObject(i);
                String id = course.getString("id");
                String name = course.getString("name");
                String description = course.optString("description", "");

                addCourseRow(id, name, description);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void addCourseRow(final String id, String name, String description) {
        TableRow row = new TableRow(this);

        TextView tvId = createTextView(id);
        TextView tvName = createTextView(name);
        TextView tvDesc = createTextView(description);

        Button btnEdit = new Button(this);
        btnEdit.setText("Изменить");
        btnEdit.setOnClickListener(v -> showEditDialog(id, name, description));

        Button btnDelete = new Button(this);
        btnDelete.setText("Удалить");
        btnDelete.setOnClickListener(v -> deleteCourse(id));

        row.addView(tvId);
        row.addView(tvName);
        row.addView(tvDesc);
        row.addView(btnEdit);
        row.addView(btnDelete);

        tableLayout.addView(row);
    }

    private void showEditDialog(String id, String name, String description) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.editcour, null);

        EditText etName = dialogView.findViewById(R.id.etName);
        EditText etDesc = dialogView.findViewById(R.id.etDescription);

        etName.setText(name);
        etDesc.setText(description);

        builder.setView(dialogView)
                .setTitle("Редактирование курса")
                .setPositiveButton("Сохранить", (dialog, which) -> {
                    String newName = etName.getText().toString();
                    String newDesc = etDesc.getText().toString();
                    updateCourse(id, newName, newDesc);
                })
                .setNegativeButton("Отмена", null)
                .create()
                .show();
    }

    private void updateCourse(String id, String name, String description) {
        JSONObject json = new JSONObject();
        try {
            json.put("id", id);
            json.put("name", name);
            json.put("description", description);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(json.toString(), JSON);
        Request request = new Request.Builder()
                .url(SUPABASE_URLCours + "?id=eq." + id)
                .patch(body)
                .addHeader("apikey", SUPABASE_API_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_API_KEY)
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "return=minimal")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(TabCours.this,
                                "Ошибка обновления", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        Toast.makeText(TabCours.this,
                                "Изменения сохранены", Toast.LENGTH_SHORT).show();
                        loadCourses();
                    }
                });
            }
        });
    }

    private void deleteCourse(String id) {
        new AlertDialog.Builder(this)
                .setTitle("Подтверждение")
                .setMessage("Удалить этот курс?")
                .setPositiveButton("Да", (dialog, which) -> {
                    Request request = new Request.Builder()
                            .url(SUPABASE_URLCours + "?id=eq." + id)
                            .delete()
                            .addHeader("apikey", SUPABASE_API_KEY)
                            .addHeader("Authorization", "Bearer " + SUPABASE_API_KEY)
                            .build();

                    client.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            runOnUiThread(() ->
                                    Toast.makeText(TabCours.this,
                                            "Ошибка удаления", Toast.LENGTH_SHORT).show());
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            runOnUiThread(() -> {
                                if (response.isSuccessful()) {
                                    Toast.makeText(TabCours.this,
                                            "Курс удален", Toast.LENGTH_SHORT).show();
                                    loadCourses();
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