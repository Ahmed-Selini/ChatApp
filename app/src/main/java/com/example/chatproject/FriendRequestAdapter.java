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

public class FriendRequestAdapter extends RecyclerView.Adapter<FriendRequestAdapter.RequestViewHolder> {

    private List<User> requestsList;
    private final OnRequestActionListener listener;

    public interface OnRequestActionListener {
        void onAccept(User sender);
        void onReject(User sender);
    }

    public FriendRequestAdapter(List<User> requestsList, OnRequestActionListener listener) {
        this.requestsList = requestsList;
        this.listener = listener;
    }

    public void updateList(List<User> newList) {
        this.requestsList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend_request, parent, false);
        return new RequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
        User user = requestsList.get(position);
        holder.bind(user, listener);
    }

    @Override
    public int getItemCount() {
        return requestsList.size();
    }

    public static class RequestViewHolder extends RecyclerView.ViewHolder {
        private final CircleImageView profileImageView;
        private final TextView tvUsername;
        private final TextView tvEmail;
        private final MaterialButton btnAccept;
        private final MaterialButton btnReject;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImageView = itemView.findViewById(R.id.requestProfileImageView);
            tvUsername = itemView.findViewById(R.id.tv_request_username);
            tvEmail = itemView.findViewById(R.id.tv_request_email);
            btnAccept = itemView.findViewById(R.id.btn_accept);
            btnReject = itemView.findViewById(R.id.btn_reject);
        }

        public void bind(final User user, final OnRequestActionListener listener) {
            tvUsername.setText(user.getUsername());
            tvEmail.setText(user.getEmail());

            // Load profile image
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

            btnAccept.setOnClickListener(v -> listener.onAccept(user));
            btnReject.setOnClickListener(v -> listener.onReject(user));
        }
    }
}