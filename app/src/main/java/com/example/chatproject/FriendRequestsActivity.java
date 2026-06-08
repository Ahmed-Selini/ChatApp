package com.example.chatproject;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class FriendRequestsActivity extends AppCompatActivity implements FriendRequestAdapter.OnRequestActionListener {

    private RecyclerView rvFriendRequests;
    private TextView tvNoRequests;
    private DatabaseHelper db;
    private int currentUserId;
    private FriendRequestAdapter adapter;
    private ImageView backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_requests);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = new DatabaseHelper(this);
        currentUserId = getIntent().getIntExtra("currentUserId", -1);

        if (currentUserId == -1) {
            Toast.makeText(this, "Authentication error.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        rvFriendRequests = findViewById(R.id.rv_friend_requests);
        tvNoRequests = findViewById(R.id.tv_no_requests);
        rvFriendRequests.setLayoutManager(new LinearLayoutManager(this));

        loadRequests();

        backButton = findViewById(R.id.back_button_blocked);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {finish();}
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        loadRequests();
    }


    private void loadRequests() {
        List<User> pendingRequests = db.getPendingFriendRequests(currentUserId);

        if (pendingRequests.isEmpty()) {
            tvNoRequests.setVisibility(View.VISIBLE);
            rvFriendRequests.setVisibility(View.GONE);
        } else {
            tvNoRequests.setVisibility(View.GONE);
            rvFriendRequests.setVisibility(View.VISIBLE);
        }

        if (adapter == null) {
            adapter = new FriendRequestAdapter(pendingRequests, this);
            rvFriendRequests.setAdapter(adapter);
        } else {
            adapter.updateList(pendingRequests);
        }
    }

    @Override
    public void onAccept(User sender) {
        boolean success = db.acceptFriendRequest(currentUserId, sender.getId());
        if (success) {
            Toast.makeText(this, sender.getUsername() + " is now your friend!", Toast.LENGTH_SHORT).show();
            loadRequests();
        } else {
            Toast.makeText(this, "Failed to accept request.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onReject(User sender) {
        boolean success = db.rejectFriendRequest(currentUserId, sender.getId());
        if (success) {
            Toast.makeText(this, "Rejected request from " + sender.getUsername(), Toast.LENGTH_SHORT).show();
            loadRequests();
        } else {
            Toast.makeText(this, "Failed to reject request.", Toast.LENGTH_SHORT).show();
        }
    }
}