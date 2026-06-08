package com.example.chatproject;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu; // Import PopupMenu
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import de.hdodenhof.circleimageview.CircleImageView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;
    private static final int VIEW_TYPE_DATE_SEPARATOR = 3;

    private final List<Object> chatItems;
    private final int currentUserId;
    private final OnMessageActionListener listener;
    private final String friendProfileImagePath;

    private final SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());

    public interface OnMessageActionListener {
        void onDelete(Message message);
        void onEdit(Message message);
    }

    public MessageAdapter(List<Object> chatItems, int currentUserId, OnMessageActionListener listener, String friendProfileImagePath) {
        this.chatItems = chatItems;
        this.currentUserId = currentUserId;
        this.listener = listener;
        this.friendProfileImagePath = friendProfileImagePath;
    }

    public void updateMessages(List<Object> newChatItems) {
        this.chatItems.clear();
        this.chatItems.addAll(newChatItems);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return chatItems.size();
    }

    @Override
    public int getItemViewType(int position) {
        Object item = chatItems.get(position);
        if (item instanceof Message) {
            Message message = (Message) item;
            return (message.getFromUserId() == currentUserId) ? VIEW_TYPE_SENT : VIEW_TYPE_RECEIVED;
        } else {
            return VIEW_TYPE_DATE_SEPARATOR;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == VIEW_TYPE_SENT) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_sent, parent, false);
            return new MessageViewHolder(view);
        } else if (viewType == VIEW_TYPE_RECEIVED) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_received, parent, false);
            return new MessageViewHolder(view);
        } else { // VIEW_TYPE_DATE_SEPARATOR
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_date_separator, parent, false);
            return new DateSeparatorViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Object item = chatItems.get(position);

        if (holder.getItemViewType() == VIEW_TYPE_DATE_SEPARATOR) {
            ((DateSeparatorViewHolder) holder).bind((String) item);
            return;
        }

        Message message = (Message) item;
        MessageViewHolder messageHolder = (MessageViewHolder) holder;

        int lastSentMessagePosition = -1;
        for (int i = chatItems.size() - 1; i >= 0; i--) {
            if (chatItems.get(i) instanceof Message &&
                    ((Message) chatItems.get(i)).getFromUserId() == currentUserId) {
                lastSentMessagePosition = i;
                break;
            }
        }

        boolean isLastSent = (position == lastSentMessagePosition);

        messageHolder.bind(message, isLastSent, friendProfileImagePath, listener, timeFormat, currentUserId);
    }

    // --- ViewHolders ---

    public static class MessageViewHolder extends RecyclerView.ViewHolder {

        TextView messageText;
        TextView tvTimestamp;
        CircleImageView ivReadReceipt;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            messageText = itemView.findViewById(R.id.message_text);
            tvTimestamp = itemView.findViewById(R.id.tv_timestamp);
            ivReadReceipt = itemView.findViewById(R.id.iv_read_receipt);
        }

        public void bind(Message message, boolean isLastSent, String friendProfileImagePath, final OnMessageActionListener listener, SimpleDateFormat timeFormat, final int currentUserId) {

            if (messageText != null) {
                messageText.setText(message.getContent());
            }

            try {
                Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(message.getTimestamp());
                if (tvTimestamp != null) {
                    tvTimestamp.setText(timeFormat.format(date));
                }
            } catch (Exception e) {
                if (tvTimestamp != null) {
                    tvTimestamp.setText("");
                }
            }

            // Read Receipt logic
            if (ivReadReceipt != null && message.getFromUserId() == currentUserId) {
                if (isLastSent && message.isRead()) {
                    ivReadReceipt.setVisibility(View.VISIBLE);

                    if (friendProfileImagePath != null && !friendProfileImagePath.isEmpty()) {
                        Glide.with(itemView.getContext())
                                .load(friendProfileImagePath.startsWith("content://") ? Uri.parse(friendProfileImagePath) : friendProfileImagePath)
                                .placeholder(R.drawable.ic_check_double)
                                .error(R.drawable.ic_check_double)
                                .into(ivReadReceipt);
                    } else {
                        ivReadReceipt.setImageResource(R.drawable.ic_check_double);
                    }
                } else {
                    ivReadReceipt.setVisibility(View.GONE);
                }
            }

            // ⭐ CORRECTED LONG PRESS LOGIC HERE ⭐
            itemView.setOnLongClickListener(v -> {
                // Only allow actions on messages sent by the current user
                if (message.getFromUserId() == currentUserId && listener != null) {

                    PopupMenu popup = new PopupMenu(v.getContext(), v);

                    // Add "Edit" and "Delete" options
                    popup.getMenu().add(0, 1, 0, "Edit");
                    popup.getMenu().add(0, 2, 1, "Delete");

                    popup.setOnMenuItemClickListener(item -> {
                        int itemId = item.getItemId();
                        if (itemId == 1) { // Edit
                            listener.onEdit(message);
                            return true;
                        } else if (itemId == 2) { // Delete
                            listener.onDelete(message);
                            return true;
                        }
                        return false;
                    });

                    popup.show();
                    return true; // Consume the long click
                }
                return false;
            });
            // ⭐ END OF CORRECTED LOGIC ⭐

            itemView.setOnClickListener(null);
        }
    }

    // ViewHolder for date separators
    class DateSeparatorViewHolder extends RecyclerView.ViewHolder {
        TextView tvSeparator;

        public DateSeparatorViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSeparator = itemView.findViewById(R.id.tv_date_separator);
            itemView.setOnClickListener(null);
            itemView.setOnLongClickListener(null);
        }

        public void bind(String dateText) {
            tvSeparator.setText(dateText);
        }
    }
}