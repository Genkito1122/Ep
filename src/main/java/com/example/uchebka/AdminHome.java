package com.example.uchebka;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class AdminHome extends AppCompatActivity {

    Button exit, Naz, Teacher;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.adminhome);

        exit = findViewById(R.id.exitat);
        Naz = findViewById(R.id.Naz);
        Teacher = findViewById(R.id.Teacher);

        exit.setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
        Naz.setOnClickListener(v -> {
            startActivity(new Intent(this, Nazz.class));
            finish();
        });
        Teacher.setOnClickListener(v -> {
            startActivity(new Intent(this, TeacherHome.class));
            finish();
        });
    }
}
