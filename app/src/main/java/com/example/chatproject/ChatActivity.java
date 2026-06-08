package com.example.chatproject;

import android.content.Context;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.emoji2.widget.EmojiEditText;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import de.hdodenhof.circleimageview.CircleImageView;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private RecyclerView rvMessages;
    private EmojiEditText etMessageInput;
    private ImageButton btnSend;
    private ImageView btnEmoji;

    private int currentUserId;
    private int friendId;
    private String friendName;

    private MessageAdapter messageAdapter;
    private Message messageToEdit = null;
    private User friendUser;

    private CircleImageView ivFriendProfile;
    private TextView tvFriendName;
    private TextView tvFriendStatus;
    private ImageView ivMenuButton;

    private View rootView;
    private InputMethodManager imm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_screen);

        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        rootView = findViewById(R.id.chat_root);

        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());
            int bottomPadding = (ime.bottom > 0) ? ime.bottom : systemBars.bottom;

            v.setPadding(
                    systemBars.left,
                    systemBars.top,
                    systemBars.right,
                    bottomPadding
            );

            return insets;
        });

        db = new DatabaseHelper(this);

        currentUserId = getIntent().getIntExtra("currentUserId", -1);
        friendId = getIntent().getIntExtra("friendId", -1);
        friendName = getIntent().getStringExtra("friendName");

        friendUser = db.getUserById(friendId);

        rvMessages = findViewById(R.id.main);
        etMessageInput = findViewById(R.id.messageInput);
        btnSend = findViewById(R.id.micro);
        btnEmoji = findViewById(R.id.btn_emoji);

        ImageView backButton = findViewById(R.id.back_button);
        ivFriendProfile = findViewById(R.id.friend_profile_image);
        tvFriendName = findViewById(R.id.friend_name);
        tvFriendStatus = findViewById(R.id.group_status);
        ivMenuButton = findViewById(R.id.menu_button);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        rvMessages.setLayoutManager(layoutManager);

        loadFriendHeaderData();
        loadMessages();
        checkBlockStatus();

        btnSend.setOnClickListener(v -> sendOrEditMessage());
        backButton.setOnClickListener(v -> finish());
        ivMenuButton.setOnClickListener(this::showPopupMenu);

        btnEmoji.setOnClickListener(v -> {
            if (etMessageInput.hasFocus()) {
                hideKeyboard();
            } else {
                showKeyboard();
            }
        });

        etMessageInput.setOnClickListener(v -> showKeyboard());
    }


    private void showKeyboard() {
        if (imm != null) {
            etMessageInput.requestFocus();
            imm.showSoftInput((View) etMessageInput, 0);
        }
    }

    private void hideKeyboard() {
        if (imm != null) {
            imm.hideSoftInputFromWindow(etMessageInput.getWindowToken(), 0);
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        friendUser = db.getUserById(friendId);
        loadMessages();
        checkBlockStatus();
        loadFriendHeaderData();
    }

    private void loadFriendHeaderData() {
        if (friendUser == null) {
            tvFriendName.setText("Utilisateur Inconnu");
            tvFriendStatus.setText("");
            ivFriendProfile.setImageResource(R.drawable.ic_group_placeholder);
            return;
        }

        tvFriendName.setText(friendUser.getUsername());

        if (friendUser.isActive()) {
            tvFriendStatus.setText("Online");
            tvFriendStatus.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark));
        } else {
            tvFriendStatus.setText("Offline");
            tvFriendStatus.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray));
        }

        String imagePath = friendUser.getProfileImagePath();
        int defaultImageResource = R.drawable.ic_group_placeholder;

        if (imagePath != null && !imagePath.isEmpty()) {
            Glide.with(this)
                    .load(imagePath)
                    .placeholder(defaultImageResource)
                    .error(defaultImageResource)
                    .into(ivFriendProfile);
        } else {
            ivFriendProfile.setImageResource(defaultImageResource);
        }
    }

    private void checkBlockStatus() {
        boolean isBlockedByCurrentUser = db.isUserBlockedBy(currentUserId, friendId);
        boolean isBlockedByFriend = db.isUserBlockedBy(friendId, currentUserId);

        View messageInputContainer = (View) etMessageInput.getParent();

        if (isBlockedByCurrentUser || isBlockedByFriend) {
            etMessageInput.setEnabled(false);
            btnSend.setEnabled(false);

            etMessageInput.setHint("You cannot send messages to this user");

            if (messageInputContainer != null) {
                messageInputContainer.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_red_light));
            }

        } else {
            etMessageInput.setEnabled(true);
            btnSend.setEnabled(true);
            etMessageInput.setHint("Aa");

            if (messageInputContainer != null) {
                messageInputContainer.setBackgroundResource(R.drawable.input_bg);
            }
        }
    }

    private static boolean isYesterday(long time) {
        return DateUtils.isToday(time + DateUtils.DAY_IN_MILLIS);
    }

    private List<Object> addDateSeparators(List<Message> messages) {
        List<Object> chatItems = new ArrayList<>();

        Calendar lastDate = null;
        SimpleDateFormat dayFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

        for (Message message : messages) {
            try {
                Date messageDate = dbFormat.parse(message.getTimestamp());
                Calendar messageCalendar = Calendar.getInstance();
                messageCalendar.setTime(messageDate);

                boolean isNewDay = (lastDate == null ||
                        messageCalendar.get(Calendar.DAY_OF_YEAR) != lastDate.get(Calendar.DAY_OF_YEAR) ||
                        messageCalendar.get(Calendar.YEAR) != lastDate.get(Calendar.YEAR));

                boolean isLongGap = (lastDate != null && !isNewDay);
                if (!isNewDay) {
                    long diff = messageCalendar.getTimeInMillis() - lastDate.getTimeInMillis();
                    if (diff >= 7200000) {
                        isLongGap = true;
                    } else {
                        isLongGap = false;
                    }
                }

                if (isNewDay || isLongGap) {
                    String dateHeader = dayFormat.format(messageDate);
                    if (DateUtils.isToday(messageDate.getTime())) {
                        dateHeader = "Aujourd'hui";
                    } else if (isYesterday(messageDate.getTime())) {
                        dateHeader = "Hier";
                    }

                    chatItems.add(dateHeader);
                }

                chatItems.add(message);
                lastDate = messageCalendar;

            } catch (ParseException e) {
                chatItems.add(message);
            }
        }
        return chatItems;
    }


    private void loadMessages() {
        db.markMessagesAsRead(currentUserId, friendId);
        List<Message> rawMessageList = db.getMessagesBetween(currentUserId, friendId);

        List<Object> chatItemsWithSeparators = addDateSeparators(rawMessageList);

        String friendProfileImagePath = friendUser != null ? friendUser.getProfileImagePath() : null;

        if (messageAdapter == null) {
            messageAdapter = new MessageAdapter(
                    chatItemsWithSeparators,
                    currentUserId,
                    new MessageAdapter.OnMessageActionListener() {
                        @Override
                        public void onDelete(Message message) {
                            db.deleteMessage(message.getId());
                            loadMessages();
                        }

                        @Override
                        public void onEdit(Message message) {
                            messageToEdit = message;
                            etMessageInput.setText(message.getContent());
                            etMessageInput.setSelection(etMessageInput.getText().length());
                            etMessageInput.requestFocus();

                            showKeyboard();
                        }
                    },
                    friendProfileImagePath
            );
            rvMessages.setAdapter(messageAdapter);
        } else {
            // Pass the mixed list to updateMessages
            messageAdapter.updateMessages(chatItemsWithSeparators);
        }

        if (!rawMessageList.isEmpty()) {
            rvMessages.scrollToPosition(chatItemsWithSeparators.size() - 1);
        }
    }

    private void sendOrEditMessage() {
        String content = etMessageInput.getText().toString().trim();
        if (content.isEmpty()) return;

        if (messageToEdit == null) {
            db.insertMessage(currentUserId, friendId, content);
        } else {
            db.updateMessage(messageToEdit.getId(), content);
            messageToEdit = null;
        }

        etMessageInput.setText("");
        loadMessages();
    }

    private void showPopupMenu(View view) {
        PopupMenu popup = new PopupMenu(this, view);
        boolean isBlocked = db.isUserBlockedBy(currentUserId, friendId);

        popup.getMenu().add(0, 1, 0, isBlocked ? "Unblock Account" : "Block Account");

        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == 1) {
                if (isBlocked) {
                    db.unblockUser(currentUserId, friendId);
                } else {
                    db.blockUser(currentUserId, friendId);
                }
                checkBlockStatus();
                return true;
            }
            return false;
        });

        popup.show();
    }
}