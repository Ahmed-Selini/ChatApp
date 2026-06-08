package com.example.chatproject;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class UserSearchActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private int currentUserId;
    private RecyclerView rvSearchResults;
    private UserSearchAdapter adapter;
    private TextView tvInstruction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_search);

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
            Toast.makeText(this, "Authentication error.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        tvInstruction = findViewById(R.id.tv_instruction);
        EditText etSearchQuery = findViewById(R.id.et_search_query);
        rvSearchResults = findViewById(R.id.rv_search_results);

        setupRecyclerView();

        etSearchQuery.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchUsers(s.toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupRecyclerView() {
        rvSearchResults.setLayoutManager(new LinearLayoutManager(this));

        adapter = new UserSearchAdapter(new ArrayList<>(), this::addFriend);
        rvSearchResults.setAdapter(adapter);
    }

    private void searchUsers(String query) {
        if (query.length() < 2) {
            adapter.updateList(new ArrayList<>());
            tvInstruction.setText("Type a username to find other users.");
            return;
        }

        List<User> results = db.searchUsers(currentUserId, query);
        adapter.updateList(results);

        if (results.isEmpty()) {
            tvInstruction.setText("No users found matching \"" + query + "\".");
        } else {
            tvInstruction.setText("Found " + results.size() + " user(s).");
        }
    }

    private void addFriend(User friend) {
        // Vérifier si l'utilisateur est déjà un ami ou si une demande est déjà en cours
        String status = db.getFriendStatus(currentUserId, friend.getId()); // Utilisateur courant -> Ami
        String reverseStatus = db.getFriendStatus(friend.getId(), currentUserId); // Ami -> Utilisateur courant

        if (status.equals("accepted") || reverseStatus.equals("accepted")) {
            Toast.makeText(this, friend.getUsername() + " is already your friend!", Toast.LENGTH_SHORT).show();
        } else if (status.equals("pending")) {
            Toast.makeText(this, "Request already sent to " + friend.getUsername() + ".", Toast.LENGTH_SHORT).show();
        } else if (reverseStatus.equals("pending")) {
            // L'ami a déjà envoyé une demande à l'utilisateur courant
            Toast.makeText(this, friend.getUsername() + " has already sent you a request. Check your requests!", Toast.LENGTH_LONG).show();
        }
        else {
            // Envoie la demande (insère un statut 'pending')
            long id = db.addFriend(currentUserId, friend.getId());

            if (id != -1) {
                Toast.makeText(this, "Friend request sent to " + friend.getUsername() + "!", Toast.LENGTH_SHORT).show();
                // Mise à jour de la liste après l'envoi
                searchUsers(((EditText) findViewById(R.id.et_search_query)).getText().toString().trim());
            } else {
                Toast.makeText(this, "Failed to send request (already added?).", Toast.LENGTH_SHORT).show();
            }
        }
    }
}