package com.example.uchebka;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class StudentHome extends AppCompatActivity {

    Button exit, Ras, Kurs;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.studenthome);

        exit = findViewById(R.id.exitst);
        Ras = findViewById(R.id.Ras);
        Kurs = findViewById(R.id.Kurs);

        exit.setOnClickListener(v -> {
            startActivity(new Intent(StudentHome.this, MainActivity.class));
            finish();
        });
        Ras.setOnClickListener(v -> {
            startActivity(new Intent(StudentHome.this, RacStudent.class));
            finish();
        });
        Kurs.setOnClickListener(v -> {
            startActivity(new Intent(StudentHome.this, KursStudent.class));
            finish();
        });
    }
}
