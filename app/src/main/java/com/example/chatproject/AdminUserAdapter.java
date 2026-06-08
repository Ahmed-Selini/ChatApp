package com.example.chatproject;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.bumptech.glide.Glide;
import de.hdodenhof.circleimageview.CircleImageView;

import java.util.List;

public class AdminUserAdapter extends RecyclerView.Adapter<AdminUserAdapter.AdminVH> {

    private List<User> users;
    private Context context;
    private DatabaseHelper db;

    public AdminUserAdapter(List<User> users, Context context, DatabaseHelper db) {
        this.users = users;
        this.context = context;
        this.db = db;
    }

    @NonNull
    @Override
    public AdminVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.item_admin_user, parent, false);
        return new AdminVH(v);
    }


    @Override
    public void onBindViewHolder(@NonNull AdminVH h, int position) {

        User u = users.get(position);

        // 🧑 Infos user
        h.tvUsername.setText(u.getUsername());
        h.tvEmail.setText(u.getEmail());

        String imagePath = u.getProfileImagePath();

        if (imagePath != null && !imagePath.isEmpty()) {
            Glide.with(context)
                    .load(imagePath)
                    .placeholder(R.drawable.ic_profile_girl)
                    .error(R.drawable.ic_profile_girl)
                    .into(h.imgUser);
        } else {
            h.imgUser.setImageResource(R.drawable.ic_profile_girl);
        }

        boolean isCurrentlyBlocked = u.isBlocked();

        if (isCurrentlyBlocked) {
            h.btnBlock.setText("UNBLOCK");
            h.btnBlock.setBackgroundTintList(context.getResources().getColorStateList(android.R.color.holo_orange_dark));
            h.btnBlock.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
        } else {
            h.btnBlock.setText("BLOCK");
            h.btnBlock.setBackgroundTintList(null);
            h.btnBlock.setTextColor(context.getResources().getColor(android.R.color.holo_orange_dark));
        }

        h.btnBlock.setOnClickListener(v -> {
            int pos = h.getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;

            User userToBlock = users.get(pos);
            boolean newState = !userToBlock.isBlocked();

            int rowsUpdated = db.setUserBlocked(userToBlock.getId(), newState);

            if (rowsUpdated > 0) {
                userToBlock.setBlocked(newState);
                notifyItemChanged(pos);

                String statusText = newState ? "blocked" : "unblocked";
                Toast.makeText(context, "User " + userToBlock.getUsername() + " has been " + statusText, Toast.LENGTH_SHORT).show();

                String subject = newState ? "Account Blocked" : "Account Unblocked";
                String body = newState ?
                        "Your account has been blocked by admin. You will not be able to log in." :
                        "Your account has been unblocked by admin. You can now log in.";

                NotificationService.sendEmail(userToBlock.getEmail(), subject, body);

            } else {
                Toast.makeText(context, "Error updating status.", Toast.LENGTH_SHORT).show();
            }
        });

        h.btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Delete Account")
                    .setMessage("Are you sure you want to delete this account? This action is irreversible.")
                    .setPositiveButton("DELETE", (dialog, which) -> {

                        int pos = h.getAdapterPosition();
                        if (pos == RecyclerView.NO_POSITION) return;

                        User userToDelete = users.get(pos);

                        boolean deleted = db.deleteUser(userToDelete.getId());

                        if (deleted) {
                            NotificationService.sendEmail(
                                    userToDelete.getEmail(),
                                    "Account Deleted",
                                    "Your account has been permanently deleted by admin."
                            );

                            users.remove(pos);
                            notifyItemRemoved(pos);

                            Toast.makeText(context, "User deleted successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, "Error deleting user.", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public void updateUsers(List<User> newList) {
        users.clear();
        users.addAll(newList);
        notifyDataSetChanged();
    }

    // ViewHolder
    static class AdminVH extends RecyclerView.ViewHolder {

        TextView tvUsername, tvEmail;
        MaterialButton btnBlock, btnDelete;
        CircleImageView imgUser;

        public AdminVH(@NonNull View itemView) {
            super(itemView);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            btnBlock = itemView.findViewById(R.id.btnBlock);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            imgUser = itemView.findViewById(R.id.imgUser);
        }
    }
}