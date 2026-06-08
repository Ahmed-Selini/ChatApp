package com.example.chatproject;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar; // Import ajouté
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class SentRequestsActivity extends AppCompatActivity implements SentRequestsAdapter.OnRequestAction {

    private DatabaseHelper db;
    private int currentUserId;
    private SentRequestsAdapter adapter;
    private TextView tvNoSentRequests; // Ajout pour gérer l'état vide
    private RecyclerView rvSentRequests; // Ajout pour la référence

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sent_requests);

        // Référence à la vue racine pour les insets (assumant R.id.main dans activity_sent_requests.xml)
        View rootLayout = findViewById(R.id.main);
        if (rootLayout == null) {
            // Si l'ID 'main' n'existe pas, utilisez la racine du layout, mais cela dépend de la structure réelle
            rootLayout = findViewById(R.id.toolbar).getParent() instanceof View ? (View) findViewById(R.id.toolbar).getParent() : findViewById(android.R.id.content);
        }

        ViewCompat.setOnApplyWindowInsetsListener(
                rootLayout,
                (v, insets) -> {
                    Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                    v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                    return insets;
                }
        );

        // Configuration de la Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        db = new DatabaseHelper(this);
        currentUserId = getIntent().getIntExtra("currentUserId", -1);

        if (currentUserId == -1) {
            Toast.makeText(this, "Authentication error.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        tvNoSentRequests = findViewById(R.id.tv_no_sent_requests); // Référence au TextView
        setupRecyclerView();
        loadSentRequests();

    }

    // Gère le clic sur le bouton Retour de la Toolbar
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }




    private void setupRecyclerView() {
        // CORRECTION: Utiliser l'ID réel du RecyclerView dans activity_sent_requests.xml
        rvSentRequests = findViewById(R.id.recycler_sent_requests);
        rvSentRequests.setLayoutManager(new LinearLayoutManager(this));

        adapter = new SentRequestsAdapter(this, this);
        rvSentRequests.setAdapter(adapter);
    }

    private void loadSentRequests() {
        // Supposition: db.getSentFriendRequests(userId) renvoie la liste des utilisateurs à qui j'ai envoyé une demande
        List<User> sentRequests = db.getSentFriendRequests(currentUserId);
        adapter.updateList(sentRequests);

        // Logique pour afficher/masquer le message d'absence de demande
        if (sentRequests.isEmpty()) {
            rvSentRequests.setVisibility(View.GONE);
            tvNoSentRequests.setVisibility(View.VISIBLE);
            // Pas de Toast ici, car le TextView fait le travail
        } else {
            rvSentRequests.setVisibility(View.VISIBLE);
            tvNoSentRequests.setVisibility(View.GONE);
        }
    }

    // Implémentation de l'interface pour retirer la demande (Étape 3)
    @Override
    public void onRemoveRequest(User user) {
        // Supposition: db.removeFriendRequest(senderId, receiverId) existe
        if (db.removeFriendRequest(currentUserId, user.getId())) {
            Toast.makeText(this, "Request to " + user.getUsername() + " cancelled.", Toast.LENGTH_SHORT).show();
            loadSentRequests(); // Recharger la liste et mettre à jour l'état vide si nécessaire
        } else {
            Toast.makeText(this, "Failed to cancel request.", Toast.LENGTH_SHORT).show();
        }
    }
}