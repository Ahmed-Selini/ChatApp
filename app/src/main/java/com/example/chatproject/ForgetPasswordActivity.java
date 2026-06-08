package com.example.chatproject;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class ForgetPasswordActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private TextInputEditText etEmailInput;
    private MaterialButton btnGoToConfirmPass; // Ajouté pour pouvoir le désactiver
    public static final String EXTRA_EMAIL = "user_email";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forget_pass);

        db = new DatabaseHelper(this);
        etEmailInput = findViewById(R.id.etEmail);
        btnGoToConfirmPass = findViewById(R.id.btnReset); // Initialisation du bouton

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
            startActivity(new Intent(ForgetPasswordActivity.this, LoginActivity.class));
        });

        btnGoToConfirmPass.setOnClickListener(v -> {
            String email = etEmailInput.getText().toString().trim();

            if (email.isEmpty()) {
                etEmailInput.setError("Email required");
                return;
            }

            if (db.getUserByEmail(email) == null) {
                Toast.makeText(this, "Email not registered.", Toast.LENGTH_SHORT).show();
                return;
            }

            String codeVerif = String.valueOf((int)(Math.random() * 900000) + 100000);

            int rowsUpdated = db.updateCodeVerif(email, codeVerif);

            if (rowsUpdated > 0) {
                // Appel de la logique asynchrone pour l'envoi d'e-mail
                sendEmailAsync(email, codeVerif);
            } else {
                Toast.makeText(this, "Error processing request.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendEmailAsync(String recipientEmail, String codeVerif) {
        Toast.makeText(this, "Sending verification code...", Toast.LENGTH_LONG).show();
        btnGoToConfirmPass.setEnabled(false); // Désactiver le bouton

        // Handler pour revenir au thread principal (UI)
        Handler uiHandler = new Handler(Looper.getMainLooper());

        new Thread(() -> {
            // L'appel bloquant se produit ici, dans le thread de travail
            boolean success = EmailSender.sendEmail(
                    recipientEmail,
                    "Password Reset Code",
                    "Your verification code is: " + codeVerif + ". Use this code to reset your password."
            );

            // Revenir au thread principal pour mettre à jour l'UI
            uiHandler.post(() -> {
                btnGoToConfirmPass.setEnabled(true); // Réactiver le bouton

                if (success) {
                    Toast.makeText(ForgetPasswordActivity.this, "Verification code sent to " + recipientEmail, Toast.LENGTH_LONG).show();

                    // Passer à l'activité suivante
                    Intent intent = new Intent(ForgetPasswordActivity.this, CodeVerificationActivity.class);
                    intent.putExtra(EXTRA_EMAIL, recipientEmail);
                    startActivity(intent);
                } else {
                    // Si l'e-mail échoue, vérifier les dépendances, le mot de passe d'application, et la connexion Internet.
                    Toast.makeText(ForgetPasswordActivity.this, "Failed to send code. Check network/App Password.", Toast.LENGTH_LONG).show();
                }
            });
        }).start();
    }
}