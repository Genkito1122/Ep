package com.example.uchebka;

import android.widget.EditText;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class Testing {

    private static final String SUPABASE_URL = "https://bvldcmhvkifayhlxjmpz.supabase.co/rest/v1/users";
    private static final String SUPABASE_API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJ2bGRjbWh2a2lmYXlobHhqbXB6Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDc4OTcxNzQsImV4cCI6MjA2MzQ3MzE3NH0.kJMQw8rAfl48W-jd2CxqPBHLBcT9ePI9wCdTImbyTrE";

    @Test
    public void testSupabaseConnection() throws InterruptedException {
        OkHttpClient client = new OkHttpClient();
        CountDownLatch latch = new CountDownLatch(1);

        Request request = new Request.Builder()
                .url(SUPABASE_URL + "?select=login")
                .get()
                .addHeader("apikey", SUPABASE_API_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_API_KEY)
                .build();

        final boolean[] success = {false};

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                success[0] = false;
                latch.countDown();
            }

            @Override
            public void onResponse(Call call, Response response) {
                success[0] = response.isSuccessful();
                latch.countDown();
            }
        });

        latch.await(5, TimeUnit.SECONDS);
        Assert.assertTrue("Подключение к Supabase не установлено", success[0]);
    }
}