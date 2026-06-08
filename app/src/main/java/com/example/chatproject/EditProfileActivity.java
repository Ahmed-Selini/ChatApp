package com.example.chatproject;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfileActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private int currentUserId;
    private User currentUser;

    // Vues
    private ImageButton backButton;
    private CircleImageView profileImage;
    private ImageView editImageButton;
    private TextInputEditText firstNameInput; // Utilisé pour Username
    private TextInputEditText emailInput;
    private Button saveButton;
    private Button changePasswordButton;

    // NOUVEAUX CHAMPS POUR LE MOT DE PASSE
    private TextInputEditText currentPasswordInput;
    private TextInputEditText newPasswordInput;

    // URI de l'image sélectionnée/actuelle
    private Uri selectedImageUri;

    // Lanceur d'activité pour la sélection d'images
    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    selectedImageUri = data.getData();
                    if (selectedImageUri != null) {
                        // Afficher l'image sélectionnée
                        profileImage.setImageURI(selectedImageUri);
                        // Persister l'URI dans le modèle User (temporairement)
                        if (currentUser != null) {
                            currentUser.setProfileImagePath(selectedImageUri.toString());
                        }
                    }
                }
            });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        db = new DatabaseHelper(this);
        currentUserId = getIntent().getIntExtra("currentUserId", -1);

        if (currentUserId == -1) {
            Toast.makeText(this, "Erreur: ID utilisateur manquant.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Initialisation des vues
        backButton = findViewById(R.id.back_button);
        profileImage = findViewById(R.id.profileImage);
        editImageButton = findViewById(R.id.editImageButton);
        firstNameInput = findViewById(R.id.firstNameInput); // Utilisé pour Username
        emailInput = findViewById(R.id.emailInput);
        saveButton = findViewById(R.id.saveButton);
        changePasswordButton = findViewById(R.id.changePasswordButton);

        // NOUVEAU: Initialisation des champs de mot de passe
        currentPasswordInput = findViewById(R.id.currentPasswordInput);
        newPasswordInput = findViewById(R.id.newPasswordInput);

        // Configuration des insets (kima tlouaaj 3liha)
        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        // 1. Charger les données de l'utilisateur
        loadUserData();

        // 2. Écouteur pour le bouton de retour
        backButton.setOnClickListener(v -> finish());

        // 3. Écouteur pour le bouton d'édition de l'image (pour ouvrir la galerie)
        editImageButton.setOnClickListener(v -> openImagePicker());

        // 4. Écouteur pour le bouton de sauvegarde
        saveButton.setOnClickListener(v -> saveProfileChanges());

        // 5. NOUVEAU: Écouteur pour le bouton Change Password
        changePasswordButton.setOnClickListener(v -> changePassword());

    }

    // ... (loadUserData, openImagePicker, saveProfileChanges restent kima 9bal)

    private void loadUserData() {
        currentUser = db.getUserById(currentUserId);

        if (currentUser != null) {
            // Afficher le username dans le champ firstNameInput (qui est maintenant le Username)
            firstNameInput.setText(currentUser.getUsername());
            emailInput.setText(currentUser.getEmail());
            // Les autres champs tna77aw

            // Charger l'image de profil (Sans changement)
            String imagePath = currentUser.getProfileImagePath();
            if (imagePath != null && !imagePath.isEmpty()) {
                selectedImageUri = Uri.parse(imagePath);
                profileImage.setImageURI(selectedImageUri);
            } else {
                // Image par défaut si aucun chemin n'est trouvé
                profileImage.setImageResource(R.drawable.ic_profile_girl);
            }
        } else {
            Toast.makeText(this, "Impossible de charger les données utilisateur.", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void saveProfileChanges() {
        // NOUVEAU: Username yji men firstNameInput barka
        String newUsername = firstNameInput.getText().toString().trim();
        String newEmail = emailInput.getText().toString().trim();
        String newImagePath = (selectedImageUri != null) ? selectedImageUri.toString() : null;

        if (newUsername.isEmpty() || newEmail.isEmpty()) {
            Toast.makeText(this, "Le nom d'utilisateur et l'e-mail sont requis.", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean success = db.updateProfile(currentUserId, newUsername, newEmail, newImagePath);

        if (success) {
            Toast.makeText(this, "Profil mis à jour avec succès !", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Échec de la mise à jour du profil.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * NOUVEAU: Logique pour changer le mot de passe
     */
    private void changePassword() {
        // Vider les erreurs précédentes
        currentPasswordInput.setError(null);
        newPasswordInput.setError(null);

        String currentPassword = currentPasswordInput.getText().toString().trim();
        String newPassword = newPasswordInput.getText().toString().trim();

        if (currentPassword.isEmpty() || newPassword.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir les deux champs de mot de passe.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentUser == null) {
            Toast.makeText(this, "Erreur: Données utilisateur introuvables.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1. Vérifier si le mot de passe actuel est correct
        // On suppose que le mot de passe dans l'objet currentUser est le mot de passe clair non haché de la DB
        if (!currentUser.getPassword().equals(currentPassword)) {
            Toast.makeText(this, "Mot de passe actuel incorrect.", Toast.LENGTH_LONG).show();
            currentPasswordInput.setError("Incorrect Password");
            return;
        }

        // 2. Vérifier si le nouveau mot de passe n'est pas l'ancien
        if (currentPassword.equals(newPassword)) {
            Toast.makeText(this, "Le nouveau mot de passe doit être différent de l'actuel.", Toast.LENGTH_LONG).show();
            newPasswordInput.setError("Same as current password");
            return;
        }

        // 3. Mise à jour du mot de passe dans la base de données
        // ATTENTION: Vous devez avoir la méthode updateUserPassword dans votre DatabaseHelper.java!
        boolean success = db.updateUserPassword(currentUser.getId(), newPassword);

        if (success) {
            Toast.makeText(this, "Mot de passe changé avec succès!", Toast.LENGTH_SHORT).show();

            // Mise à jour de l'objet currentUser en mémoire
            // (Il faut ajouter un setPassword dans la classe User)
            // currentUser.setPassword(newPassword);

            // Vider les champs
            currentPasswordInput.setText("");
            newPasswordInput.setText("");
        } else {
            Toast.makeText(this, "Échec du changement de mot de passe. (Vérifiez DatabaseHelper)", Toast.LENGTH_SHORT).show();
        }
    }
}