package com.example.chatproject;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.List;

public class BlockedUserAdapter extends RecyclerView.Adapter<BlockedUserAdapter.BlockedUserViewHolder> {

    private List<User> blockedUsersList;
    private final Context context;
    private final int currentUserId;
    private final DatabaseHelper db;
    private final Runnable onUserUnblocked;

    private static final int DEFAULT_IMAGE_RESOURCE = R.drawable.ic_profile_girl;

    public BlockedUserAdapter(Context context, List<User> blockedUsersList, int currentUserId, DatabaseHelper db, Runnable onUserUnblocked) {
        this.context = context;
        this.blockedUsersList = blockedUsersList;
        this.currentUserId = currentUserId;
        this.db = db;
        this.onUserUnblocked = onUserUnblocked;
    }

    public void updateList(List<User> newList) {
        this.blockedUsersList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BlockedUserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_blocked_user, parent, false);
        return new BlockedUserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BlockedUserViewHolder holder, int position) {
        User blockedUser = blockedUsersList.get(position);
        holder.tvUsername.setText(blockedUser.getUsername());

        String profileImagePath = blockedUser.getProfileImagePath();

        if (profileImagePath != null && !profileImagePath.isEmpty()) {
            try {
                Glide.with(context)
                        .load(profileImagePath)
                        .placeholder(DEFAULT_IMAGE_RESOURCE)
                        .error(DEFAULT_IMAGE_RESOURCE)
                        .into(holder.ivAvatar);
            } catch (Exception e) {
                Log.e("BlockedUserAdapter", "Error loading profile image for user: " + blockedUser.getUsername(), e);
                holder.ivAvatar.setImageResource(DEFAULT_IMAGE_RESOURCE);
            }
        } else {
            holder.ivAvatar.setImageResource(DEFAULT_IMAGE_RESOURCE);
        }


        holder.btn_unblock.setOnClickListener(v -> {
            unblockUser(blockedUser);
        });
    }

    @Override
    public int getItemCount() {
        return blockedUsersList.size();
    }

    private void unblockUser(User userToUnblock) {

        boolean success = db.unblockUser(currentUserId, userToUnblock.getId());

        if (success) {
            Toast.makeText(context, userToUnblock.getUsername() + " has been unblocked.", Toast.LENGTH_SHORT).show();

            if (onUserUnblocked != null) {
                onUserUnblocked.run();
            }
        } else {
            Toast.makeText(context, "Failed to unblock " + userToUnblock.getUsername(), Toast.LENGTH_SHORT).show();
        }
    }

    static class BlockedUserViewHolder extends RecyclerView.ViewHolder {
        TextView tvUsername;
        ImageView ivAvatar;
        ImageView btn_unblock;

        public BlockedUserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUsername = itemView.findViewById(R.id.blocked_username);
            ivAvatar = itemView.findViewById(R.id.blocked_profile_image);
            btn_unblock = itemView.findViewById(R.id.btn_unblock);
        }
    }
}