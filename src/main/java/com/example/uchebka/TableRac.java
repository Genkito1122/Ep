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

public class TableRac extends AppCompatActivity {

    private static final String SUPABASE_URLLessons = "https://bvldcmhvkifayhlxjmpz.supabase.co/rest/v1/lessons";
    private static final String SUPABASE_API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJ2bGRjbWh2a2lmYXlobHhqbXB6Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDc4OTcxNzQsImV4cCI6MjA2MzQ3MzE3NH0.kJMQw8rAfl48W-jd2CxqPBHLBcT9ePI9wCdTImbyTrE";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private TableLayout tableLayout;
    private Button btnBack, btnRefresh;
    private OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tablerac);

        tableLayout = findViewById(R.id.tableLayout);
        btnBack = findViewById(R.id.btnBack);
        btnRefresh = findViewById(R.id.btnRefresh);

        btnBack.setOnClickListener(v -> {
            startActivity(new Intent(this, RacTeacher.class));
            finish();
        });

        btnRefresh.setOnClickListener(v -> loadSchedule());

        loadSchedule();
    }

    private void loadSchedule() {
        tableLayout.removeAllViews();
        addTableHeader();

        String url = SUPABASE_URLLessons + "?select=id,date,time,description,courses(name)";
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
                        Toast.makeText(TableRac.this, "Ошибка загрузки расписания", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    if (response.isSuccessful()) {
                        JSONArray scheduleArray = new JSONArray(response.body().string());
                        runOnUiThread(() -> {
                            try {
                                for (int i = 0; i < scheduleArray.length(); i++) {
                                    JSONObject lesson = scheduleArray.getJSONObject(i);
                                    String id = lesson.getString("id");
                                    String date = lesson.getString("date");
                                    String time = lesson.getString("time");
                                    String course = lesson.getJSONObject("courses").getString("name");
                                    String desc = lesson.optString("description", "");
                                    addTableRow(id, date, time, course, desc);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void addTableHeader() {
        TableRow headerRow = new TableRow(this);

        TextView headerDate = createHeaderTextView("Дата");
        TextView headerTime = createHeaderTextView("Время");
        TextView headerCourse = createHeaderTextView("Курс");
        TextView headerDesc = createHeaderTextView("Описание");
        TextView headerActions = createHeaderTextView("Действия");

        headerRow.addView(headerDate);
        headerRow.addView(headerTime);
        headerRow.addView(headerCourse);
        headerRow.addView(headerDesc);
        headerRow.addView(headerActions);

        tableLayout.addView(headerRow);
    }

    private TextView createHeaderTextView(String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setPadding(16, 16, 16, 16);
        tv.setTextSize(16);
        tv.setTextColor(getResources().getColor(android.R.color.white));
        tv.setBackgroundColor(getResources().getColor(R.color.purple_500));
        return tv;
    }

    private void addTableRow(final String id, String date, String time, String course, String desc) {
        TableRow row = new TableRow(this);

        TextView tvDate = createTextView(date);
        TextView tvTime = createTextView(time);
        TextView tvCourse = createTextView(course);
        TextView tvDesc = createTextView(desc);

        Button btnEdit = new Button(this);
        btnEdit.setText("Изменить");
        btnEdit.setBackgroundColor(getResources().getColor(R.color.purple_200));
        btnEdit.setOnClickListener(v -> showEditDialog(id, date, time, course, desc));

        Button btnDelete = new Button(this);
        btnDelete.setText("Удалить");
        btnDelete.setBackgroundColor(getResources().getColor(R.color.purple_200));
        btnDelete.setOnClickListener(v -> showDeleteConfirmation(id));

        TableRow.LayoutParams params = new TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT);
        params.setMargins(4, 4, 4, 4);

        row.addView(tvDate, params);
        row.addView(tvTime, params);
        row.addView(tvCourse, params);
        row.addView(tvDesc, params);

        TableRow actionsContainer = new TableRow(this);
        actionsContainer.addView(btnEdit, params);
        actionsContainer.addView(btnDelete, params);
        row.addView(actionsContainer);

        tableLayout.addView(row);
    }

    private void showEditDialog(String id, String date, String time, String course, String desc) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.editrac, null);

        EditText etDate = dialogView.findViewById(R.id.etDate);
        EditText etTime = dialogView.findViewById(R.id.etTime);
        EditText etDesc = dialogView.findViewById(R.id.etDescription);

        etDate.setText(date);
        etTime.setText(time);
        etDesc.setText(desc);

        builder.setView(dialogView)
                .setTitle("Редактирование занятия")
                .setPositiveButton("Сохранить", (dialog, which) -> {
                    String newDate = etDate.getText().toString();
                    String newTime = etTime.getText().toString();
                    String newDesc = etDesc.getText().toString();
                    updateLesson(id, newDate, newTime, newDesc);
                })
                .setNegativeButton("Отмена", null)
                .create()
                .show();
    }

    private void updateLesson(String id, String date, String time, String description) {
        try {
            JSONObject json = new JSONObject();
            json.put("date", date);
            json.put("time", time);
            json.put("description", description);

            RequestBody body = RequestBody.create(json.toString(), JSON);
            Request request = new Request.Builder()
                    .url(SUPABASE_URLLessons + "?id=eq." + id)
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
                            Toast.makeText(TableRac.this, "Ошибка обновления", Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    runOnUiThread(() -> {
                        if (response.isSuccessful()) {
                            Toast.makeText(TableRac.this, "Изменения сохранены", Toast.LENGTH_SHORT).show();
                            loadSchedule();
                        }
                    });
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void showDeleteConfirmation(String id) {
        new AlertDialog.Builder(this)
                .setTitle("Подтверждение удаления")
                .setMessage("Вы уверены, что хотите удалить это занятие?")
                .setPositiveButton("Удалить", (dialog, which) -> deleteLesson(id))
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void deleteLesson(String id) {
        Request request = new Request.Builder()
                .url(SUPABASE_URLLessons + "?id=eq." + id)
                .delete()
                .addHeader("apikey", SUPABASE_API_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_API_KEY)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(TableRac.this, "Ошибка удаления", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        Toast.makeText(TableRac.this, "Занятие удалено", Toast.LENGTH_SHORT).show();
                        loadSchedule();
                    }
                });
            }
        });
    }

    private TextView createTextView(String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setPadding(16, 16, 16, 16);
        tv.setTextSize(14);
        tv.setBackgroundColor(getResources().getColor(R.color.purple_100));
        return tv;
    }
}