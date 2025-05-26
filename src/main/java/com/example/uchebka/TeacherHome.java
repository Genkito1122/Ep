package com.example.uchebka;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class TeacherHome extends AppCompatActivity {

    Button exit, Courses, Grades, racTeacher, ZapCourses;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.teacherhome);

        exit = findViewById(R.id.exitat);
        Courses = findViewById(R.id.Courses);
        Grades = findViewById(R.id.Grades);
        ZapCourses = findViewById(R.id.ZapCourses);
        racTeacher = findViewById(R.id.racteacher);

        exit.setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
        racTeacher.setOnClickListener(v -> {
            startActivity(new Intent(this, RacTeacher.class));
            finish();
        });
        Courses.setOnClickListener(v -> {
            startActivity(new Intent(this, AddCourse.class));
            finish();
        });
        Grades.setOnClickListener(v -> {
            startActivity(new Intent(this, Grade.class));
            finish();
        });
        ZapCourses.setOnClickListener(v -> {
            startActivity(new Intent(this, TabEnroll.class));
            finish();
        });
    }
}
