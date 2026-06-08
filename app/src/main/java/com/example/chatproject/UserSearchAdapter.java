package com.example.chatproject;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserSearchAdapter extends RecyclerView.Adapter<UserSearchAdapter.UserSearchViewHolder> {

    private List<User> usersList;
    private final OnAddFriendListener listener;

    public interface OnAddFriendListener {
        void onAddFriend(User user);
    }

    public UserSearchAdapter(List<User> usersList, OnAddFriendListener listener) {
        this.usersList = usersList;
        this.listener = listener;
    }

    public void updateList(List<User> newList) {
        this.usersList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserSearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_search, parent, false);
        return new UserSearchViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserSearchViewHolder holder, int position) {
        User user = usersList.get(position);
        holder.bind(user, listener);
    }

    @Override
    public int getItemCount() {
        return usersList.size();
    }

    public static class UserSearchViewHolder extends RecyclerView.ViewHolder {
        private final CircleImageView searchProfileImageView;
        private final TextView tvUsername;
        private final MaterialButton btnAddFriend;

        public UserSearchViewHolder(@NonNull View itemView) {
            super(itemView);
            searchProfileImageView = itemView.findViewById(R.id.searchProfileImageView);
            tvUsername = itemView.findViewById(R.id.tv_username);
            btnAddFriend = itemView.findViewById(R.id.btn_add_friend);
        }

        public void bind(final User user, final OnAddFriendListener listener) {

            // FIX 1: RESTAURER L'AFFICHAGE du nom d'utilisateur et de l'email
            tvUsername.setText(String.format("%s (%s)", user.getUsername(), user.getEmail()));

            // LOGIQUE DE CHARGEMENT DE L'IMAGE (avec try-catch pour la stabilité)
            String imagePath = user.getProfileImagePath();
            if (imagePath != null && !imagePath.isEmpty()) {

                try {
                    searchProfileImageView.setImageURI(Uri.parse(imagePath));
                } catch (SecurityException e) {
                    searchProfileImageView.setImageResource(R.drawable.ic_profile_girl);
                } catch (Exception e) {
                    searchProfileImageView.setImageResource(R.drawable.ic_profile_girl);
                }
            } else {
                searchProfileImageView.setImageResource(R.drawable.ic_profile_girl);
            }

            // FIX 2: RESTAURER LE LISTENER du bouton "Add Friend" pour que l'ajout fonctionne
            btnAddFriend.setOnClickListener(v -> listener.onAddFriend(user));
        }
    }
}