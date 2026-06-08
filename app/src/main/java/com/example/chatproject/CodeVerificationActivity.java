package com.example.chatproject;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;

public class CodeVerificationActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.confirm_code);

        db = new DatabaseHelper(this);

        userEmail = getIntent().getStringExtra(ForgetPasswordActivity.EXTRA_EMAIL);
        if (userEmail == null) {
            Toast.makeText(this, "Error: Email not provided.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        setupClickListeners();
    }

    private void setupClickListeners() {

        TextView tvBack = findViewById(R.id.tvBackToLogin);
        tvBack.setOnClickListener(v -> {
            startActivity(new Intent(CodeVerificationActivity.this, LoginActivity.class));
        });

        MaterialButton btnVerify = findViewById(R.id.btnVerify);
        btnVerify.setOnClickListener(v -> {

            String code = getOtpCode();

            if (code.length() != 6) {
                Toast.makeText(this, "Please enter the 6-digit code.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (db.checkCodeVerif(userEmail, code)) {

                Toast.makeText(this, "Code verified successfully!", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(CodeVerificationActivity.this, ResetPasswordActivity.class);
                intent.putExtra(ForgetPasswordActivity.EXTRA_EMAIL, userEmail);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Invalid verification code.", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private String getOtpCode() {

        String code1 = ((EditText) findViewById(R.id.etOtp1)).getText().toString();
        String code2 = ((EditText) findViewById(R.id.etOtp2)).getText().toString();
        String code3 = ((EditText) findViewById(R.id.etOtp3)).getText().toString();
        String code4 = ((EditText) findViewById(R.id.etOtp4)).getText().toString();
        String code5 = ((EditText) findViewById(R.id.etOtp5)).getText().toString();
        String code6 = ((EditText) findViewById(R.id.etOtp6)).getText().toString();

        return code1 + code2 + code3 + code4 + code5 + code6;
    }
}