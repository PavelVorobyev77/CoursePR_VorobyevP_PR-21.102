package com.example.coursepr_vorobyevp_pr_21102;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Устанавливаем макет для SplashActivity
        setContentView(R.layout.activity_splash);

        // Логика загрузки данных или проверки состояния
        // Предполагаем, что данные загружены успешно через 2 секунды
        new Handler().postDelayed(() -> {
            Intent mainIntent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(mainIntent);
            finish(); // Закрываем SplashActivity
        }, 1000); // Задержка 1 секунда
    }
}