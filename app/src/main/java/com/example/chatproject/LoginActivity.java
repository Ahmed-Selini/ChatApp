package com.example.chatproject;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {

    private DatabaseHelper db;
    // ADMIN CREDENTIALS (HARDCODED)
    private static final String ADMIN_EMAIL = "admin@chatlina.com";
    private static final String ADMIN_PASSWORD = "admin123";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_account);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = new DatabaseHelper(this);

        setupClickListeners();
    }

    private void setupClickListeners() {

        TextView tvSignup = findViewById(R.id.tvSignup);
        tvSignup.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, CreateAccountActivity.class));
        });

        MaterialButton btnLogin = findViewById(R.id.btnSign);

        btnLogin.setOnClickListener(v -> {

            String email = ((TextInputEditText) findViewById(R.id.etUsername)).getText().toString().trim();
            String password = ((TextInputEditText) findViewById(R.id.etPassword)).getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter your email and password", Toast.LENGTH_SHORT).show();
                return;
            }

            if (email.equals(ADMIN_EMAIL)) {
                if (password.equals(ADMIN_PASSWORD)) {
                    Intent intent = new Intent(LoginActivity.this, AdminActivity.class);
                    startActivity(intent);
                    finish();
                    return;
                } else {
                    Toast.makeText(this, "Invalid credentials for Admin.", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            User user = db.getUserByEmail(email);

            if (user != null) {
                // ⭐ AJOUT DE LA VÉRIFICATION DE BLOCAGE ⭐
                if (user.isBlocked()) {
                    Toast.makeText(this, "Your account has been blocked.", Toast.LENGTH_LONG).show();
                    return; // Empêche la connexion si l'utilisateur est bloqué
                }
                // ⭐ FIN DE L'AJOUT ⭐

                if (user.getPassword().equals(password)) {
                    db.setUserActive(user.getId(), true);

                    Intent intent = new Intent(LoginActivity.this, MessageListActivity.class);
                    intent.putExtra("userId", user.getId());
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(this, "Invalid password.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "User not found for this email.", Toast.LENGTH_SHORT).show();
            }
        });

        TextView tvReset = findViewById(R.id.tvForgot);
        tvReset.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, ForgetPasswordActivity.class));
        });
    }
}