package com.example.chatproject;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;

public class CreateAccountActivity extends AppCompatActivity {

    TextInputEditText etName, etPassword, etEmail;
    Button btnCreate;
    DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_account);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = new DatabaseHelper(this);

        etName = findViewById(R.id.etName);
        etPassword = findViewById(R.id.etPassword);
        etEmail = findViewById(R.id.etEmail);

        btnCreate = findViewById(R.id.btnCreate);

        btnCreate.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            String email = (etEmail != null && etEmail.getText() != null) ? etEmail.getText().toString().trim() : "";

            if (name.isEmpty()) {
                etName.setError("Required");
                return;
            }
            if (password.isEmpty()) {
                etPassword.setError("Required");
                return;
            }

            if (email.isEmpty()) {
                email = name.replaceAll("\\s+","").toLowerCase() + "@local.com";
            }

            String codeVerif = String.valueOf((int)(Math.random()*900000)+100000);

            long id = db.insertUser(name, email, password, codeVerif);

            if (id != -1) {

                Toast.makeText(this, "Account created successfully! Please log in.", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(CreateAccountActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);

                finish();
            } else {
                Toast.makeText(this, "Error creating account (maybe email exists)", Toast.LENGTH_LONG).show();
            }
        });

        TextView tvLogin = findViewById(R.id.tvLogin);
        tvLogin.setOnClickListener(v -> {
            Intent intent = new Intent(CreateAccountActivity.this, LoginActivity.class);
            startActivity(intent);
        });
    }
}