package com.example.chatproject;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.view.MenuItem;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import de.hdodenhof.circleimageview.CircleImageView;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

// L'interface OnChatsUpdatedListener est supposée être dans son propre fichier.

public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.FriendViewHolder> {

    // Utilisation d'une seule liste car le filtrage est géré par l'Activity et lui passe la liste filtrée/complète
    private List<User> friendsList;
    private final OnFriendClickListener listener;
    private final DatabaseHelper db;
    private final int currentUserId;
    private final OnChatsUpdatedListener updateListener;

    public interface OnFriendClickListener {
        void onFriendClick(User friend);
    }

    /**
     * CONSTRUCTEUR MIS À JOUR
     * @param friendsList La liste des amis à afficher (peut être filtrée)
     * @param updateListener L'écouteur pour signaler à l'activité de rafraîchir la liste après suppression.
     */
    public FriendAdapter(List<User> friendsList, OnFriendClickListener listener, DatabaseHelper db, int currentUserId, OnChatsUpdatedListener updateListener) {
        this.friendsList = friendsList;
        this.listener = listener;
        this.db = db;
        this.currentUserId = currentUserId;
        this.updateListener = updateListener;
    }

    // NOUVELLE MÉTHODE POUR LA RECHERCHE (Étape 1)
    // Utilisée par MessageListActivity pour appliquer les résultats du filtre.
    public void updateFriendsList(List<User> newList) {
        this.friendsList = newList;
        notifyDataSetChanged();
    }

    // NOUVELLE MÉTHODE pour mettre à jour la liste complète lorsque l'on revient de l'activité
    // ou après une suppression dans l'activité (utilisé par setupRecycler dans MessageListActivity).
    public void setOriginalFriendsList(List<User> newOriginalList) {
        this.friendsList = newOriginalList;
        notifyDataSetChanged(); // Important pour s'assurer que la vue est rafraîchie
    }


    @NonNull
    @Override
    public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_row, parent, false);
        return new FriendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendViewHolder holder, int position) {
        User friend = friendsList.get(position);

        // Binding normal
        holder.bind(friend, listener, db, currentUserId);

        // NOUVEAU : Long Click Listener pour la suppression de conversation (Étape 2)
        holder.itemView.setOnLongClickListener(v -> {
            showDeleteConversationPopup(v, friend);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return friendsList.size();
    }

    // NOUVELLE MÉTHODE POUR LA SUPPRESSION DE CONVERSATION (Étape 2)
    private void showDeleteConversationPopup(View v, User friend) {
        PopupMenu popup = new PopupMenu(v.getContext(), v);
        // Utilisation d'un ID temporaire 5001 pour "Delete Conversation"
        popup.getMenu().add(0, 5001, 0, "Delete Conversation");

        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == 5001) {
                new AlertDialog.Builder(v.getContext())
                        .setTitle("Confirm Deletion")
                        .setMessage("Are you sure you want to delete all messages with " + friend.getUsername() + " and remove the chat from the list?")
                        .setPositiveButton("Yes, Delete", (dialog, which) -> {
                            // Supprimer les messages et la relation d'amitié (méthode db.deleteConversationAndFriendship doit être ajoutée)
                            // NOTE: J'utilise 'deleteConversation' qui est plus clair et englobe les messages + la relation.
                            boolean deleted = db.deleteConversation(currentUserId, friend.getId());

                            if (deleted) {
                                Toast.makeText(v.getContext(), "Conversation with " + friend.getUsername() + " deleted.", Toast.LENGTH_SHORT).show();
                                // Informer l'activité de recharger la liste
                                if (updateListener != null) {
                                    updateListener.onChatsUpdated();
                                }
                            } else {
                                Toast.makeText(v.getContext(), "Failed to delete conversation. Please try again.", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
                return true;
            }
            return false;
        });
        popup.show();
    }


    public static class FriendViewHolder extends RecyclerView.ViewHolder {

        private final CircleImageView ivProfilePic;
        private final TextView tvUsername;
        private final TextView tvLastMessage;
        private final TextView tvTimestamp;
        private final View onlineStatusIndicator;
        private final TextView tvUnreadCount;
        private final CircleImageView ivReadStatus;

        public FriendViewHolder(@NonNull View itemView) {
            super(itemView);

            ivProfilePic = itemView.findViewById(R.id.iv_profile_pic);
            tvUsername = itemView.findViewById(R.id.tv_username);
            tvLastMessage = itemView.findViewById(R.id.tv_last_message);
            tvTimestamp = itemView.findViewById(R.id.tv_timestamp);
            onlineStatusIndicator = itemView.findViewById(R.id.online_status_indicator);
            tvUnreadCount = itemView.findViewById(R.id.tv_unread_count);
            ivReadStatus = itemView.findViewById(R.id.iv_read_status);
        }

        /**
         * Converts the SQLite timestamp (YYYY-MM-DD HH:mm:ss) to a display time (hh:mm a).
         */
        private String formatTimestampToTime(String timestamp) {
            if (timestamp == null || timestamp.isEmpty()) return "";

            SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            SimpleDateFormat displayFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());

            try {
                Date date = dbFormat.parse(timestamp);
                return displayFormat.format(date);
            } catch (ParseException e) {
                // Si la date est mal formatée, essaie de retourner les 5 derniers caractères (probablement HH:mm)
                return timestamp.length() > 5 ? timestamp.substring(timestamp.length() - 5) : "";
            } catch (Exception e) {
                return "";
            }
        }


        @SuppressLint("SetTextI18n")
        public void bind(final User friend, final OnFriendClickListener listener, final DatabaseHelper db, final int currentUserId) {
            tvUsername.setText(friend.getUsername());

            // 1. Profile Image Loading
            String imagePath = friend.getProfileImagePath();
            int defaultImage = R.drawable.ic_profile_girl;

            if (imagePath != null && !imagePath.isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(imagePath.startsWith("content://") ? Uri.parse(imagePath) : imagePath)
                        .placeholder(defaultImage)
                        .error(defaultImage)
                        .into(ivProfilePic);
            } else {
                ivProfilePic.setImageResource(defaultImage);
            }

            // 2. Last Message and Status Logic
            if (db != null) {
                // NOTE: La classe LastMessageInfo est supposée exister dans votre projet.
                LastMessageInfo lastMessage = db.getLastMessageForChat(currentUserId, friend.getId());
                int unreadCount = db.getUnreadMessageCount(currentUserId, friend.getId());

                if (lastMessage == null || lastMessage.getContent().isEmpty()) {
                    tvLastMessage.setText("Say hello! 👋");
                    tvTimestamp.setText("");
                    ivReadStatus.setVisibility(View.GONE);
                } else {
                    tvLastMessage.setText(lastMessage.getContent());

                    tvTimestamp.setText(formatTimestampToTime(lastMessage.getTimestamp()));

                    // Read Receipt Logic
                    if (lastMessage.isLastSentMessageRead()) {
                        ivReadStatus.setVisibility(View.VISIBLE);

                        // Charge l'image de l'ami sur l'icône de lecture
                        Glide.with(itemView.getContext())
                                .load(imagePath.startsWith("content://") ? Uri.parse(imagePath) : imagePath)
                                .placeholder(R.drawable.ic_check_double)
                                .error(R.drawable.ic_check_double)
                                .into(ivReadStatus);

                    } else {
                        ivReadStatus.setVisibility(View.GONE);
                    }
                }

                // Display Unread Badge for RECEIVED unread messages
                if (unreadCount > 0) {
                    tvUnreadCount.setVisibility(View.VISIBLE);
                    tvUnreadCount.setText(String.valueOf(unreadCount));
                    tvLastMessage.setTextColor(tvLastMessage.getResources().getColor(R.color.text_black, null));
                } else {
                    tvUnreadCount.setVisibility(View.GONE);
                    tvLastMessage.setTextColor(tvLastMessage.getResources().getColor(R.color.text_gray, null));
                }
            } else {
                tvLastMessage.setText("[DB Error]");
                tvTimestamp.setText("");
                tvUnreadCount.setVisibility(View.GONE);
                ivReadStatus.setVisibility(View.GONE);
            }

            // 3. Online Status Indicator
            onlineStatusIndicator.setVisibility(friend.isActive() ? View.VISIBLE : View.GONE);

            // Click court pour ouvrir le chat
            itemView.setOnClickListener(v -> {
                listener.onFriendClick(friend);
            });
        }
    }
}