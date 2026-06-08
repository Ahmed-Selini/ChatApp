package com.example.chatproject;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.graphics.Insets;

import java.util.ArrayList;
import java.util.List;

public class BlockedUsersActivity extends AppCompatActivity {

    private RecyclerView rvBlockedUsers;
    private DatabaseHelper db;
    private int currentUserId = -1;
    private BlockedUserAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blocked_users);

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main),
                (v, insets) -> {
                    Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                    v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                    return insets;
                }
        );

        db = new DatabaseHelper(this);

        currentUserId = getIntent().getIntExtra("currentUserId", -1);
        if (currentUserId == -1) {
            Toast.makeText(this, "User ID not found. Please log in again.", Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        ImageView backButton = findViewById(R.id.back_button_blocked);
        backButton.setOnClickListener(v -> finish());

        setupRecycler();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (currentUserId != -1) {
            loadBlockedUsers();
        }
    }

    private void setupRecycler() {
        rvBlockedUsers = findViewById(R.id.rv_blocked_users);
        rvBlockedUsers.setLayoutManager(new LinearLayoutManager(this));

        adapter = new BlockedUserAdapter(this, new ArrayList<>(), currentUserId, db, this::loadBlockedUsers);
        rvBlockedUsers.setAdapter(adapter);

        loadBlockedUsers();
    }

    private void loadBlockedUsers() {
        List<User> blockedList = db.getBlockedUsers(currentUserId);

        adapter.updateList(blockedList);

        if (blockedList.isEmpty()) {
            Toast.makeText(this, "You have not blocked any users.", Toast.LENGTH_LONG).show();
        }
    }
}