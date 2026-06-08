package com.example.chatproject;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;

public class AddAccountActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private TextInputEditText etName, etEmail, etPassword;
    private ImageButton backButton;
    private android.widget.Button btnAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_account);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = new DatabaseHelper(this);

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        backButton = findViewById(R.id.back_button);
        btnAdd = findViewById(R.id.btnAdd);

        setupClickListeners();
    }

    private void setupClickListeners() {

        backButton.setOnClickListener(v -> finish());

        btnAdd.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "All fields are required.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (db.getUserByEmail(email) != null) {
                Toast.makeText(this, "An account already exists with this email.", Toast.LENGTH_LONG).show();
                return;
            }

            String codeVerif = "0000";

            long newId = db.insertUser(name, email, password, codeVerif);

            if (newId != -1) {

                Toast.makeText(this, "User " + name + " created successfully.", Toast.LENGTH_LONG).show();

                EmailSender.sendEmail(
                        email,
                        "Account Created by Admin",
                        "Your chat account has been created by the administrator. Your initial password is: " + password
                );

                finish();
            } else {
                Toast.makeText(this, "Error creating account.", Toast.LENGTH_LONG).show();
            }
        });
    }
}