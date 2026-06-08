package com.example.chatproject;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import de.hdodenhof.circleimageview.CircleImageView;

public class SentRequestsAdapter extends RecyclerView.Adapter<SentRequestsAdapter.SentRequestViewHolder> {

    private List<User> usersList = new ArrayList<>();
    private final OnRequestAction listener;
    private final Context context;

    public interface OnRequestAction {
        void onRemoveRequest(User user);
    }

    public SentRequestsAdapter(Context context, OnRequestAction listener) {
        this.context = context;
        this.listener = listener;
    }

    public void updateList(List<User> newList) {
        this.usersList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SentRequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Utiliser une mise en page similaire ou créer item_sent_request.xml
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_search, parent, false);
        return new SentRequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SentRequestViewHolder holder, int position) {
        User user = usersList.get(position);
        holder.bind(user, listener);
    }

    @Override
    public int getItemCount() {
        return usersList.size();
    }

    public static class SentRequestViewHolder extends RecyclerView.ViewHolder {
        private final CircleImageView profileImageView;
        private final TextView tvUsername;
        private final Button btnRemoveRequest; // Utiliser le bouton existant (si possible)

        public SentRequestViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImageView = itemView.findViewById(R.id.searchProfileImageView);
            tvUsername = itemView.findViewById(R.id.tv_username);
            // Assumer que vous réutilisez le bouton 'btn_add_friend' pour 'Remove' et vous changez son texte
            btnRemoveRequest = itemView.findViewById(R.id.btn_add_friend);
        }

        public void bind(final User user, final OnRequestAction listener) {
            tvUsername.setText(String.format("%s (%s)", user.getUsername(), user.getEmail()));
            btnRemoveRequest.setText("Cancel Request");
            btnRemoveRequest.setOnClickListener(v -> listener.onRemoveRequest(user));

            // Logique de chargement de l'image
            String imagePath = user.getProfileImagePath();
            if (imagePath != null && !imagePath.isEmpty()) {
                try {
                    profileImageView.setImageURI(Uri.parse(imagePath));
                } catch (Exception e) {
                    profileImageView.setImageResource(R.drawable.ic_profile_girl);
                }
            } else {
                profileImageView.setImageResource(R.drawable.ic_profile_girl);
            }
        }
    }
}