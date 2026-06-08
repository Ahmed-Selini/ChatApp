package com.example.chatproject;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;


import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private TextView appNameTextView;
    private String textToWrite = "ChatLina";
    private int textIndex = 0;
    private long charDelay = 150;
    private Handler animationHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        appNameTextView = findViewById(R.id.appNameTextView);
        animateText();
    }

    private Runnable characterAdder = new Runnable() {
        @Override
        public void run() {
            if (textIndex < textToWrite.length()) {

                appNameTextView.setText(textToWrite.substring(0, textIndex + 1));
                textIndex++;

                animationHandler.postDelayed(this, charDelay);

            } else {

                animationHandler.postDelayed(() -> {

                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(intent);

                    overridePendingTransition(
                            android.R.anim.fade_in,
                            android.R.anim.fade_out
                    );

                    finish();

                }, 1000);
            }
        }
    };

    private void animateText() {
        textIndex = 0;
        appNameTextView.setText("");
        animationHandler.postDelayed(characterAdder, charDelay);
    }
}
