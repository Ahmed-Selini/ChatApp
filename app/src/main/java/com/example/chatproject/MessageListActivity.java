package com.example.chatproject;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import android.net.Uri;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.util.Log;

// IMPLEMENTE L'INTERFACE POUR LA MISE À JOUR (Suppression de chat)
public class MessageListActivity extends AppCompatActivity implements OnChatsUpdatedListener {

    private RecyclerView rvChats;
    private DatabaseHelper db;
    private int currentUserId = -1;

    private ImageView btnSettings;
    private TextView tvRequestCount;
    private EditText etSearchChats;

    private FriendAdapter adapter;
    private List<User> allActiveFriends; // Cache de la liste complète des amis

    private static final int READ_MEDIA_REQUEST_CODE = 1001;
    // IDs pour les menus
    private static final int RECEIVED_REQUESTS_ID = 4001;
    private static final int SENT_REQUESTS_ID = 4002;
    private static final int DELETE_ACCOUNT_ID = 6001;

    private static final String CHANNEL_ID = "chat_messages_channel";
    private static final int NOTIFICATION_ID = 101;
    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 1002;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Assurez-vous que R.layout.message_list correspond à votre fichier XML
        setContentView(R.layout.message_list);

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main),
                (v, insets) -> {
                    Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                    v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                    return insets;
                }
        );

        db = new DatabaseHelper(this);

        currentUserId = getIntent().getIntExtra("userId", -1);
        if (currentUserId == -1) {
            Toast.makeText(this, "User ID not found. Please log in again.", Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        createNotificationChannel();

        btnSettings = findViewById(R.id.btn_profile_settings);
        btnSettings.setOnClickListener(this::showSettingsPopup);

        tvRequestCount = findViewById(R.id.tv_request_count);
        // Utilise la nouvelle méthode de menu pour les demandes
        findViewById(R.id.btn_friend_requests_container).setOnClickListener(this::showFriendRequestsPopup);

        setupFab();
        setupSearch(); // Configuration de la barre de recherche

        checkAndRequestImagePermission();

    }

    // Mise en œuvre de l'interface (Étape 2)
    @Override
    public void onChatsUpdated() {
        setupRecycler(); // Recharge le RecyclerView après une suppression de chat
        updateFriendRequestCount();
    }

    // NOUVELLE MÉTHODE (Étape 1)
    private void setupSearch() {
        // L'ID doit correspondre à celui dans message_list.xml
        etSearchChats = findViewById(R.id.et_search_chats);

        etSearchChats.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterChats(s.toString().trim().toLowerCase());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    // NOUVELLE MÉTHODE (Étape 1)
    private void filterChats(String query) {
        // Si l'adaptateur ou la liste complète n'est pas encore initialisée, on arrête
        if (adapter == null || allActiveFriends == null) return;

        List<User> filteredList = new ArrayList<>();
        if (query.isEmpty()) {
            filteredList.addAll(allActiveFriends);
        } else {
            for (User user : allActiveFriends) {
                // Filtrer par nom d'utilisateur ou email
                if (user.getUsername().toLowerCase().contains(query) || user.getEmail().toLowerCase().contains(query)) {
                    filteredList.add(user);
                }
            }
        }
        // Mise à jour de la liste dans l'adaptateur
        adapter.updateFriendsList(filteredList);
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (currentUserId != -1) {
            // Réinitialiser la recherche lors du retour
            if (etSearchChats != null) etSearchChats.setText("");
            setupRecycler();
            updateFriendRequestCount();
            checkForUnreadMessagesAndNotify();
        }
    }

    private void updateFriendRequestCount() {
        if (currentUserId == -1 || tvRequestCount == null) return;

        int count = db.getPendingFriendRequests(currentUserId).size();

        if (count > 0) {
            tvRequestCount.setText(String.valueOf(count));
            tvRequestCount.setVisibility(View.VISIBLE);
        } else {
            tvRequestCount.setVisibility(View.GONE);
        }
    }

    // NOUVELLE MÉTHODE: Popup pour choisir Request Recues ou Envoyées (Étape 3)
    private void showFriendRequestsPopup(View v) {
        PopupMenu popup = new PopupMenu(this, v);

        popup.getMenu().add(0, RECEIVED_REQUESTS_ID, 0, "Received Requests");
        popup.getMenu().add(0, SENT_REQUESTS_ID, 1, "Sent Requests");

        popup.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == RECEIVED_REQUESTS_ID) {
                goToFriendRequests();
                return true;
            } else if (itemId == SENT_REQUESTS_ID) {
                goToSentRequests();
                return true;
            }
            return false;
        });
        popup.show();
    }

    // Ancien goToFriendRequests (maintenant pour les demandes reçues)
    private void goToFriendRequests() {
        Intent intent = new Intent(MessageListActivity.this, FriendRequestsActivity.class);
        intent.putExtra("currentUserId", currentUserId);
        startActivity(intent);
    }

    // NOUVELLE MÉTHODE (Étape 3)
    private void goToSentRequests() {
        Intent intent = new Intent(MessageListActivity.this, SentRequestsActivity.class);
        intent.putExtra("currentUserId", currentUserId);
        startActivity(intent);
    }


    private void showSettingsPopup(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        popup.getMenuInflater().inflate(R.menu.menu_settings, popup.getMenu());

        // AJOUT DU BOUTON "Supprimer le compte" (Étape 4)
        popup.getMenu().add(0, DELETE_ACCOUNT_ID, 100, "Delete Account");

        popup.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.menu_edit_profile) {
                goToEditProfile();
                return true;
            }
            else if (itemId == R.id.menu_blocked_users) {
                goToBlockedUsers();
                return true;
            }
            else if (itemId == R.id.menu_logout) {
                logoutUser();
                return true;
            }
            else if (itemId == DELETE_ACCOUNT_ID) { // Delete Account (Étape 4)
                deleteAccount();
                return true;
            }
            return false;
        });
        popup.show();
    }

    // NOUVELLE MÉTHODE (Étape 4)
    private void deleteAccount() {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Account Deletion")
                .setMessage("Are you sure you want to permanently delete your account? This action cannot be undone. All your messages and data will be erased.")
                .setPositiveButton("Yes, Delete", (dialog, which) -> {
                    // db.deleteUser doit être implémenté dans DatabaseHelper
                    if (db.deleteUser(currentUserId)) {
                        Toast.makeText(this, "Your account has been successfully deleted.", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(MessageListActivity.this, LoginActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(this, "Failed to delete account. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }


    private void setupRecycler() {
        rvChats = findViewById(R.id.rv_chats);
        rvChats.setLayoutManager(new LinearLayoutManager(this));

        List<User> friendsList = db.getFriends(currentUserId);

        List<User> blockedUsers = db.getBlockedUsers(currentUserId);
        Set<Integer> blockedIds = new HashSet<>();
        for (User blockedUser : blockedUsers) {
            blockedIds.add(blockedUser.getId());
        }

        allActiveFriends = new ArrayList<>();
        for (User friend : friendsList) {
            if (!blockedIds.contains(friend.getId())) {
                allActiveFriends.add(friend);
            }
        }

        // Si l'adaptateur existe déjà, on met juste à jour la liste complète pour la recherche/filtrage
        if (adapter == null) {
            adapter = new FriendAdapter(allActiveFriends, friend -> {
                Intent i = new Intent(MessageListActivity.this, ChatActivity.class);
                i.putExtra("currentUserId", currentUserId);
                i.putExtra("friendId", friend.getId());
                i.putExtra("friendName", friend.getUsername());
                startActivity(i);
            }, db, currentUserId, this); // PASSAGE DE 'this' pour l'écouteur de suppression

            rvChats.setAdapter(adapter);
        } else {
            // Mettre à jour la liste complète de l'adaptateur et réappliquer le filtre si nécessaire
            adapter.setOriginalFriendsList(allActiveFriends);
            filterChats(etSearchChats.getText().toString().trim().toLowerCase());
        }


        if (allActiveFriends.isEmpty() && (etSearchChats == null || etSearchChats.getText().toString().isEmpty())) {
            Toast.makeText(this, "You have no active chats. Add some friends!", Toast.LENGTH_LONG).show();
        }
    }

    // ... (checkAndRequestImagePermission, onRequestPermissionsResult, goToBlockedUsers, goToEditProfile, logoutUser, setupFab - INCHANGÉES) ...

    private void goToBlockedUsers() {
        Intent intent = new Intent(MessageListActivity.this, BlockedUsersActivity.class);
        intent.putExtra("currentUserId", currentUserId);
        startActivity(intent);
    }

    private void goToEditProfile() {
        Intent intent = new Intent(MessageListActivity.this, EditProfileActivity.class);
        intent.putExtra("currentUserId", currentUserId);
        startActivity(intent);
    }

    private void logoutUser() {
        if (currentUserId != -1) {
            db.setUserActive(currentUserId, false);
        }

        Intent intent = new Intent(MessageListActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

        Toast.makeText(this, "Logged out successfully.", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void setupFab() {
        FloatingActionButton fab = findViewById(R.id.fab_new_chat);
        fab.setOnClickListener(v -> {
            Intent i = new Intent(MessageListActivity.this, UserSearchActivity.class);
            i.putExtra("currentUserId", currentUserId);
            startActivity(i);
        });
    }

    private void checkAndRequestImagePermission() {
        String permission;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permission = Manifest.permission.READ_MEDIA_IMAGES;
        } else {
            permission = Manifest.permission.READ_EXTERNAL_STORAGE;
        }

        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{permission}, READ_MEDIA_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == READ_MEDIA_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission accordée
            } else {
                Toast.makeText(this, "Image access denied. Avatars will not be visible.", Toast.LENGTH_LONG).show();
            }
        }

        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission accordée, réessayer la vérification
                checkForUnreadMessagesAndNotify();
            } else {
                Toast.makeText(this, "Notification permission denied. You might miss new messages.", Toast.LENGTH_LONG).show();
            }
        }

    }

    // ⭐ NOUVELLE MÉTHODE : Création du canal de notification ⭐
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Utilise les chaînes définies dans strings.xml
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    // ⭐ NOUVELLE MÉTHODE : Logique de vérification et de notification ⭐
    // ⭐ MÉTHODE MODIFIÉE : Logique de vérification et de notification avec Avatar ⭐
    private void checkForUnreadMessagesAndNotify() {

        // --- 1. Gestion de la permission POST_NOTIFICATIONS (Android 13+) ---
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION_REQUEST_CODE);
                return;
            }
        }

        List<Message> unreadMessages = db.getUnreadMessagesForUser(currentUserId);

        if (unreadMessages.isEmpty()) {
            NotificationManagerCompat.from(this).cancel(NOTIFICATION_ID);
            return;
        }

        Message latestMessage = unreadMessages.get(unreadMessages.size() - 1);
        int senderId = latestMessage.getFromUserId();
        User sender = db.getUser(senderId);

        if (sender == null) return;

        String senderName = sender.getUsername();
        String messageContent = latestMessage.getContent();
        int totalUnreadCount = unreadMessages.size();

        // --- 2. Préparation de l'Intent (Action lors du clic) ---
        Intent chatIntent = new Intent(this, ChatActivity.class);
        chatIntent.putExtra("currentUserId", currentUserId);
        chatIntent.putExtra("friendId", senderId);
        chatIntent.putExtra("friendName", senderName);
        chatIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                senderId,
                chatIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // ⭐ ÉTAPE CLÉ : Récupérer et convertir l'image de profil ⭐
        Bitmap largeIconBitmap = null;
        String imagePath = sender.getProfileImagePath();

        if (imagePath != null && !imagePath.isEmpty()) {
            try {
                // Utiliser ContentResolver pour charger l'URI en Bitmap
                // (Nécessite ContextCompat.checkSelfPermission pour READ_MEDIA_IMAGES/READ_EXTERNAL_STORAGE)
                Uri imageUri = Uri.parse(imagePath);

                // Utilisez getContext().getContentResolver()
                largeIconBitmap = MediaStore.Images.Media.getBitmap(
                        this.getContentResolver(),
                        imageUri
                );

                // Optionnel : Redimensionner le Bitmap à une taille appropriée (environ 96dp pour les notifications)
                if (largeIconBitmap != null) {
                    largeIconBitmap = Bitmap.createScaledBitmap(
                            largeIconBitmap,
                            96,
                            96,
                            false
                    );
                }

            } catch (Exception e) {
                Log.e("Notification", "Erreur lors du chargement de l'avatar: " + e.getMessage());
                largeIconBitmap = null; // En cas d'erreur, on n'utilise pas d'avatar
            }
        }
        // ⭐ FIN ÉTAPE CLÉ ⭐

        // --- 3. Construction de la Notification ---
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification) // Petite icône monochrome obligatoire
                .setContentTitle(senderName)
                .setContentText(messageContent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setWhen(System.currentTimeMillis());

        // ⭐ NOUVEAU : Ajouter le grand icône (l'avatar) ⭐
        if (largeIconBitmap != null) {
            builder.setLargeIcon(largeIconBitmap);
        }
        // --- (Style Inbox existant) ---
        if (totalUnreadCount > 1) {
            NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
            inboxStyle.setBigContentTitle(senderName + " (" + totalUnreadCount + " nouveaux messages)");

            for (int i = Math.max(0, unreadMessages.size() - 5); i < unreadMessages.size(); i++) {
                inboxStyle.addLine(unreadMessages.get(i).getContent());
            }
            builder.setStyle(inboxStyle)
                    .setSubText(totalUnreadCount + " messages non lus");
        }

        // --- 4. Affichage ---
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

}