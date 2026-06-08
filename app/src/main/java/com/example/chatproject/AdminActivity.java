package com.example.chatproject;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class AdminActivity extends AppCompatActivity {

    private RecyclerView rvUsers;
    private EditText etSearchUsers;

    private DatabaseHelper db;
    private AdminUserAdapter adapter;

    private List<User> allUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        db = new DatabaseHelper(this);

        rvUsers = findViewById(R.id.rvUsers);
        etSearchUsers = findViewById(R.id.et_search_users);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        rvUsers.setLayoutManager(new LinearLayoutManager(this));

        loadUsers();
        setupSearch();
        setupLogout();
        setupAddUser();
    }

    private void loadUsers() {
        allUsers = db.searchUsers(-1, "");
        adapter = new AdminUserAdapter(new ArrayList<>(allUsers), this, db);
        rvUsers.setAdapter(adapter);
    }

    private void setupSearch() {
        etSearchUsers.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterUsers(s.toString().trim().toLowerCase());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterUsers(String query) {
        if (adapter == null || allUsers == null) return;

        List<User> filteredList = new ArrayList<>();

        if (query.isEmpty()) {
            filteredList.addAll(allUsers);
        } else {
            for (User user : allUsers) {
                if (
                        user.getUsername().toLowerCase().contains(query) ||
                                user.getEmail().toLowerCase().contains(query)
                ) {
                    filteredList.add(user);
                }
            }
        }

        adapter.updateUsers(filteredList);
    }

    private void setupAddUser() {
        FloatingActionButton fabAddUser = findViewById(R.id.fab_add_user);

        fabAddUser.setOnClickListener(v -> {
            startActivity(new Intent(AdminActivity.this, AddAccountActivity.class));
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (etSearchUsers != null) etSearchUsers.setText("");
        loadUsers();
    }

    private void setupLogout() {
        ImageView btnLogout = findViewById(R.id.btn_admin_logout);

        btnLogout.setOnClickListener(v -> {
            Intent intent = new Intent(AdminActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

}
