package com.example.chatproject;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "chat_app.db";
    public static final int DATABASE_VERSION = 7;

    public static final String TABLE_USER = "users";
    public static final String COL_USER_ID = "iduser";
    public static final String COL_USERNAME = "username";
    public static final String COL_EMAIL = "email";
    public static final String COL_PASSWORD = "password";
    public static final String COL_ISACTIVE = "isactive";
    public static final String COL_CODEVERIF = "codeverifpass";
    public static final String COL_PROFILE_IMAGE = "profile_image_path";
    public static final String COL_IS_BLOCKED = "is_blocked";


    public static final String TABLE_MESSAGE = "messages";
    public static final String COL_MESSAGE_ID = "idmessage";
    public static final String COL_MESSAGE_USER_ID = "iduser";
    public static final String COL_MESSAGE_TO_ID = "to_user_id";
    public static final String COL_MESSAGE_CONTENT = "content";
    public static final String COL_MESSAGE_TIMESTAMP = "timestamp";
    public static final String COL_IS_READ = "is_read";

    public static final String TABLE_FRIEND = "friends";
    public static final String COL_FRIEND_ID = "id";
    public static final String COL_FRIEND_USER = "user_id";
    public static final String COL_FRIEND_FRIEND = "friend_user_id";
    public static final String COL_FRIEND_STATUS = "status";

    public static final String TABLE_BLOCKED = "blocked_list";
    public static final String COL_BLOCKED_ID = "id";
    public static final String COL_BLOCKER_ID = "blocker_user_id";
    public static final String COL_BLOCKED_TARGET_ID = "blocked_target_id";


    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    private User cursorToUser(Cursor c) {
        try {
            return new User(
                    c.getInt(c.getColumnIndexOrThrow(COL_USER_ID)),
                    c.getString(c.getColumnIndexOrThrow(COL_USERNAME)),
                    c.getString(c.getColumnIndexOrThrow(COL_EMAIL)),
                    c.getString(c.getColumnIndexOrThrow(COL_PASSWORD)),
                    c.getInt(c.getColumnIndexOrThrow(COL_ISACTIVE)) == 1,
                    c.getString(c.getColumnIndexOrThrow(COL_CODEVERIF)),
                    c.getString(c.getColumnIndexOrThrow(COL_PROFILE_IMAGE)),
                    c.getInt(c.getColumnIndexOrThrow(COL_IS_BLOCKED)) == 1);
        } catch (IllegalArgumentException e) {
            Log.e("DatabaseHelper", "Error in cursorToUser: Missing column. Did you update DATABASE_VERSION?", e);
            throw e;
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_USERS = "CREATE TABLE " + TABLE_USER + " ("
                + COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_USERNAME + " TEXT NOT NULL, "
                + COL_EMAIL + " TEXT UNIQUE NOT NULL, "
                + COL_PASSWORD + " TEXT NOT NULL, "
                + COL_ISACTIVE + " INTEGER DEFAULT 0, "
                + COL_CODEVERIF + " TEXT, "
                + COL_PROFILE_IMAGE + " TEXT DEFAULT NULL, "
                + COL_IS_BLOCKED + " INTEGER DEFAULT 0"
                + ");";

        String CREATE_MESSAGES = "CREATE TABLE " + TABLE_MESSAGE + " ("
                + COL_MESSAGE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_MESSAGE_USER_ID + " INTEGER NOT NULL, "
                + COL_MESSAGE_TO_ID + " INTEGER NOT NULL, "
                + COL_MESSAGE_CONTENT + " TEXT, "
                + COL_MESSAGE_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP, "
                + COL_IS_READ + " INTEGER DEFAULT 0, "
                + "FOREIGN KEY(" + COL_MESSAGE_USER_ID + ") REFERENCES " + TABLE_USER + "(" + COL_USER_ID + "), "
                + "FOREIGN KEY(" + COL_MESSAGE_TO_ID + ") REFERENCES " + TABLE_USER + "(" + COL_USER_ID + ")"
                + ");";

        String CREATE_FRIENDS = "CREATE TABLE " + TABLE_FRIEND + " ("
                + COL_FRIEND_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_FRIEND_USER + " INTEGER NOT NULL, "
                + COL_FRIEND_FRIEND + " INTEGER NOT NULL, "
                + COL_FRIEND_STATUS + " TEXT DEFAULT 'accepted', "
                + "UNIQUE(" + COL_FRIEND_USER + "," + COL_FRIEND_FRIEND + "), "
                + "FOREIGN KEY(" + COL_FRIEND_USER + ") REFERENCES " + TABLE_USER + "(" + COL_USER_ID + "), "
                + "FOREIGN KEY(" + COL_FRIEND_FRIEND + ") REFERENCES " + TABLE_USER + "(" + COL_USER_ID + ")"
                + ");";

        String CREATE_BLOCKED_LIST = "CREATE TABLE " + TABLE_BLOCKED + " ("
                + COL_BLOCKED_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_BLOCKER_ID + " INTEGER NOT NULL, "
                + COL_BLOCKED_TARGET_ID + " INTEGER NOT NULL, "
                + "UNIQUE(" + COL_BLOCKER_ID + "," + COL_BLOCKED_TARGET_ID + "), "
                + "FOREIGN KEY(" + COL_BLOCKER_ID + ") REFERENCES " + TABLE_USER + "(" + COL_USER_ID + "), "
                + "FOREIGN KEY(" + COL_BLOCKED_TARGET_ID + ") REFERENCES " + TABLE_USER + "(" + COL_USER_ID + ")"
                + ");";


        db.execSQL(CREATE_USERS);
        db.execSQL(CREATE_MESSAGES);
        db.execSQL(CREATE_FRIENDS);
        db.execSQL(CREATE_BLOCKED_LIST);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            try {
                String ADD_COLUMN_PROFILE_IMAGE = "ALTER TABLE " + TABLE_USER +
                        " ADD COLUMN " + COL_PROFILE_IMAGE + " TEXT DEFAULT NULL;";
                db.execSQL(ADD_COLUMN_PROFILE_IMAGE);
            } catch (Exception e) {
                Log.e("DatabaseHelper", "Failed to add column " + COL_PROFILE_IMAGE + ": " + e.getMessage());
            }
        }

        if (oldVersion < 3) {
            try {
                String ADD_COLUMN_IS_BLOCKED = "ALTER TABLE " + TABLE_USER +
                        " ADD COLUMN " + COL_IS_BLOCKED + " INTEGER DEFAULT 0;";
                db.execSQL(ADD_COLUMN_IS_BLOCKED);
            } catch (Exception e) {
                Log.e("DatabaseHelper", "Failed to add column " + COL_IS_BLOCKED + ": " + e.getMessage());
            }
        }

        if (oldVersion < 5 && newVersion >= 5) {
            String CREATE_BLOCKED_LIST = "CREATE TABLE IF NOT EXISTS " + TABLE_BLOCKED + " ("
                    + COL_BLOCKED_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + COL_BLOCKER_ID + " INTEGER NOT NULL, "
                    + COL_BLOCKED_TARGET_ID + " INTEGER NOT NULL, "
                    + "UNIQUE(" + COL_BLOCKER_ID + "," + COL_BLOCKED_TARGET_ID + "), "
                    + "FOREIGN KEY(" + COL_BLOCKER_ID + ") REFERENCES " + TABLE_USER + "(" + COL_USER_ID + "), "
                    + "FOREIGN KEY(" + COL_BLOCKED_TARGET_ID + ") REFERENCES " + TABLE_USER + "(" + COL_USER_ID + ")"
                    + ");";
            db.execSQL(CREATE_BLOCKED_LIST);
            Log.d("DatabaseHelper", "Upgrading to V5: Blocked List Table created/verified.");
        }

        if (oldVersion < 6 && newVersion >= 6) {
            try {
                String ADD_COLUMN_IS_READ = "ALTER TABLE " + TABLE_MESSAGE +
                        " ADD COLUMN " + COL_IS_READ + " INTEGER DEFAULT 0;";
                db.execSQL(ADD_COLUMN_IS_READ);
                Log.d("DatabaseHelper", "Upgrading to V6: Added COL_IS_READ to messages table.");
            } catch (Exception e) {
                Log.e("DatabaseHelper", "Failed to add column " + COL_IS_READ + ": " + e.getMessage());
            }
        }

    }

    public long insertUser(String username, String email, String password, String codeVerif) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_USERNAME, username);
        cv.put(COL_EMAIL, email);
        cv.put(COL_PASSWORD, password);
        cv.put(COL_CODEVERIF, codeVerif);
        cv.put(COL_PROFILE_IMAGE, (String) null);
        cv.put(COL_IS_BLOCKED, 0);
        long id = db.insert(TABLE_USER, null, cv);
        db.close();
        return id;
    }

    public User getUserByEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.query(TABLE_USER, null, COL_EMAIL + "=?",
                new String[]{email}, null, null, null);
        if (c != null && c.moveToFirst()) {
            User u = cursorToUser(c);
            c.close();
            db.close();
            return u;
        }
        if (c != null) c.close();
        db.close();
        return null;
    }

    public User getUserById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.query(TABLE_USER, null, COL_USER_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null);
        if (c != null && c.moveToFirst()) {
            User u = cursorToUser(c);
            c.close();
            db.close();
            return u;
        }
        if (c != null) c.close();
        db.close();
        return null;
    }

    public boolean updateProfile(int userId, String newUsername, String newEmail, String newImagePath) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_USERNAME, newUsername);
        cv.put(COL_EMAIL, newEmail);
        cv.put(COL_PROFILE_IMAGE, newImagePath);

        int rows = db.update(TABLE_USER, cv, COL_USER_ID + "=?", new String[]{String.valueOf(userId)});
        db.close();
        return rows > 0;
    }

    public int setUserActive(int userId, boolean active) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_ISACTIVE, active ? 1 : 0);
        int rows = db.update(TABLE_USER, cv, COL_USER_ID + "=?", new String[]{String.valueOf(userId)});
        db.close();
        return rows;
    }

    public int updateCodeVerif(String email, String newCode) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_CODEVERIF, newCode);
        int rows = db.update(TABLE_USER, cv, COL_EMAIL + "=?", new String[]{email});
        db.close();
        return rows;
    }

    public boolean checkCodeVerif(String email, String code) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.query(TABLE_USER, new String[]{COL_USER_ID}, COL_EMAIL + "=? AND " + COL_CODEVERIF + "=?",
                new String[]{email, code}, null, null, null);
        boolean success = (c != null && c.getCount() > 0);
        if (c != null) c.close();
        db.close();
        return success;
    }

    public int updatePassword(String email, String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_PASSWORD, newPassword);
        cv.put(COL_CODEVERIF, (String) null); // Clear the verification code after use
        int rows = db.update(TABLE_USER, cv, COL_EMAIL + "=?", new String[]{email});
        db.close();
        return rows;
    }

    public boolean updateUserPassword(int userId, String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_PASSWORD, newPassword);
        int rows = db.update(TABLE_USER, cv, COL_USER_ID + "=?", new String[]{String.valueOf(userId)});
        db.close();
        return rows > 0;
    }

    public long insertMessage(int fromUserId, int toUserId, String content) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_MESSAGE_USER_ID, fromUserId);
        cv.put(COL_MESSAGE_TO_ID, toUserId);
        cv.put(COL_MESSAGE_CONTENT, content);
        cv.put(COL_IS_READ, 0);
        long id = db.insert(TABLE_MESSAGE, null, cv);
        db.close();
        return id;
    }

    public int getUnreadMessageCount(int currentUserId, int friendId) {
        int count = 0;
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT COUNT(*) FROM " + TABLE_MESSAGE +
                " WHERE " + COL_MESSAGE_USER_ID + " = ? " +
                " AND " + COL_MESSAGE_TO_ID + " = ? " +
                " AND " + COL_IS_READ + " = 0";

        String[] selectionArgs = new String[]{String.valueOf(friendId), String.valueOf(currentUserId)};

        Cursor c = db.rawQuery(query, selectionArgs);
        if (c.moveToFirst()) {
            count = c.getInt(0);
        }
        c.close();
        db.close();
        return count;
    }

    public int markMessagesAsRead(int currentUserId, int friendId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_IS_READ, 1);

        String whereClause = COL_MESSAGE_USER_ID + " = ? AND " + COL_MESSAGE_TO_ID + " = ? AND " + COL_IS_READ + " = 0";
        String[] whereArgs = new String[]{String.valueOf(friendId), String.valueOf(currentUserId)};

        int rowsUpdated = db.update(TABLE_MESSAGE, values, whereClause, whereArgs);
        db.close();

        Log.d("DatabaseHelper", rowsUpdated + " messages marked as read in chat with " + friendId);
        return rowsUpdated;
    }


    public boolean updateMessage(int messageId, String newContent) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_MESSAGE_CONTENT, newContent);

        int rows = db.update(TABLE_MESSAGE, values, COL_MESSAGE_ID + "=?", new String[]{String.valueOf(messageId)});
        db.close();
        return rows > 0;
    }

    public boolean deleteMessage(int messageId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(TABLE_MESSAGE, COL_MESSAGE_ID + "=?", new String[]{String.valueOf(messageId)});
        db.close();
        return rows > 0;
    }

    public List<Message> getMessagesBetween(int userA, int userB) {
        List<Message> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String sql = "SELECT *, " + COL_IS_READ + " FROM " + TABLE_MESSAGE + " WHERE (" +
                COL_MESSAGE_USER_ID + "=" + userA + " AND " + COL_MESSAGE_TO_ID + "=" + userB +
                ") OR (" + COL_MESSAGE_USER_ID + "=" + userB + " AND " + COL_MESSAGE_TO_ID + "=" + userA +
                ") ORDER BY " + COL_MESSAGE_TIMESTAMP + " ASC";

        Cursor c = db.rawQuery(sql, null);
        if (c.moveToFirst()) {
            do {

                @SuppressLint("Range")
                boolean isRead = c.getInt(c.getColumnIndexOrThrow(COL_IS_READ)) == 1;

                Message m = new Message(
                        c.getInt(c.getColumnIndexOrThrow(COL_MESSAGE_ID)),
                        c.getInt(c.getColumnIndexOrThrow(COL_MESSAGE_USER_ID)),
                        c.getInt(c.getColumnIndexOrThrow(COL_MESSAGE_TO_ID)),
                        c.getString(c.getColumnIndexOrThrow(COL_MESSAGE_CONTENT)),
                        c.getString(c.getColumnIndexOrThrow(COL_MESSAGE_TIMESTAMP)),
                        isRead
                );
                list.add(m);
            } while (c.moveToNext());
        }
        c.close();
        db.close();
        return list;
    }

    private String getFriendStatusInternal(SQLiteDatabase db, int userId, int friendId) {
        String status = "none";
        Cursor c = null;

        String query = "SELECT " + COL_FRIEND_STATUS + " FROM " + TABLE_FRIEND +
                " WHERE " + COL_FRIEND_USER + " = ? AND " + COL_FRIEND_FRIEND + " = ?";

        String[] selectionArgs = new String[]{String.valueOf(userId), String.valueOf(friendId)};

        try {
            c = db.rawQuery(query, selectionArgs);

            if (c.moveToFirst()) {
                @SuppressLint("Range") String currentStatus = c.getString(c.getColumnIndexOrThrow(COL_FRIEND_STATUS));
                status = currentStatus != null ? currentStatus : "none";
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error in getFriendStatusInternal: " + e.getMessage());
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return status;
    }

    public String getFriendStatus(int userId, int friendId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String status = getFriendStatusInternal(db, userId, friendId);
        if (db != null && db.isOpen()) {
            db.close();
        }
        return status;
    }

    public List<User> getPendingFriendRequests(int currentUserId) {
        List<User> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String sql = "SELECT u.* FROM " + TABLE_USER + " u INNER JOIN " + TABLE_FRIEND +
                " f ON u." + COL_USER_ID + " = f." + COL_FRIEND_USER +
                " WHERE f." + COL_FRIEND_FRIEND + " = ? AND f." + COL_FRIEND_STATUS + " = 'pending'";

        Cursor c = db.rawQuery(sql, new String[]{String.valueOf(currentUserId)});
        if (c.moveToFirst()) {
            do {
                User u = cursorToUser(c);
                list.add(u);
            } while (c.moveToNext());
        }
        c.close();
        db.close();
        return list;
    }

    public boolean acceptFriendRequest(int currentUserId, int senderId) {
        SQLiteDatabase db = this.getWritableDatabase();
        boolean success = false;
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(COL_FRIEND_STATUS, "accepted");

            int rowsUpdated = db.update(TABLE_FRIEND, values,
                    COL_FRIEND_USER + " = ? AND " + COL_FRIEND_FRIEND + " = ? AND " + COL_FRIEND_STATUS + " = 'pending'",
                    new String[]{String.valueOf(senderId), String.valueOf(currentUserId)});

            if (rowsUpdated > 0) {
                ContentValues cvInverse = new ContentValues();
                cvInverse.put(COL_FRIEND_USER, currentUserId);
                cvInverse.put(COL_FRIEND_FRIEND, senderId);
                cvInverse.put(COL_FRIEND_STATUS, "accepted");
                long id = db.insertOrThrow(TABLE_FRIEND, null, cvInverse);

                if (id != -1) {
                    db.setTransactionSuccessful();
                    success = true;
                }
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error accepting friend request", e);
        } finally {
            db.endTransaction();
            db.close();
        }
        return success;
    }

    public boolean rejectFriendRequest(int currentUserId, int senderId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsDeleted = db.delete(TABLE_FRIEND,
                COL_FRIEND_USER + " = ? AND " + COL_FRIEND_FRIEND + " = ? AND " + COL_FRIEND_STATUS + " = 'pending'",
                new String[]{String.valueOf(senderId), String.valueOf(currentUserId)});
        db.close();
        return rowsDeleted > 0;
    }


    public long addFriend(int userId, int friendId) {
        SQLiteDatabase db = this.getWritableDatabase();
        long id = -1;

        db.beginTransaction();
        try {
            String status = getFriendStatusInternal(db, userId, friendId);
            String reverseStatus = getFriendStatusInternal(db, friendId, userId);

            boolean alreadyFriends = status.equals("accepted") || reverseStatus.equals("accepted");
            boolean requestPending = status.equals("pending") || reverseStatus.equals("pending");

            if (!alreadyFriends && !requestPending) {
                ContentValues cvA = new ContentValues();
                cvA.put(COL_FRIEND_USER, userId);
                cvA.put(COL_FRIEND_FRIEND, friendId);
                cvA.put(COL_FRIEND_STATUS, "pending");

                id = db.insert(TABLE_FRIEND, null, cvA);

                if (id != -1) {
                    db.setTransactionSuccessful();
                }
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error adding friend request: " + e.getMessage());
        } finally {
            db.endTransaction();
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
        return id;
    }

    public List<User> getFriends(int userId) {
        List<User> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String sql = "SELECT u.* FROM " + TABLE_USER + " u INNER JOIN " + TABLE_FRIEND +
                " f ON u." + COL_USER_ID + " = f." + COL_FRIEND_FRIEND +
                " WHERE f." + COL_FRIEND_USER + " = ? AND f." + COL_FRIEND_STATUS + " = 'accepted'" +
                " ORDER BY u." + COL_USERNAME + " ASC";

        String[] selectionArgs = new String[]{String.valueOf(userId)};

        Cursor c = db.rawQuery(sql, selectionArgs);

        if (c.moveToFirst()) {
            do {
                User u = cursorToUser(c);
                list.add(u);
            } while (c.moveToNext());
        }
        c.close();
        db.close();
        return list;
    }

    public List<User> searchUsers(int currentUserId, String query) {
        List<User> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String likeQuery = "%" + query + "%";

        String sql = "SELECT u.* FROM " + TABLE_USER + " u " +
                "WHERE u." + COL_USERNAME + " LIKE ? " +
                "AND u." + COL_USER_ID + " != ? " +
                "AND u." + COL_USER_ID + " NOT IN (" +

                "    SELECT " + COL_FRIEND_FRIEND + " FROM " + TABLE_FRIEND +
                "    WHERE " + COL_FRIEND_USER + " = ? " +

                " UNION " +

                "    SELECT " + COL_FRIEND_USER + " FROM " + TABLE_FRIEND +
                "    WHERE " + COL_FRIEND_FRIEND + " = ? " +

                ") " +
                "ORDER BY u." + COL_USERNAME + " ASC";

        String[] selectionArgs = new String[]{
                likeQuery,
                String.valueOf(currentUserId),
                String.valueOf(currentUserId),
                String.valueOf(currentUserId)
        };

        Cursor c = db.rawQuery(sql, selectionArgs);

        if (c.moveToFirst()) {
            do {
                User u = cursorToUser(c);
                list.add(u);
            } while (c.moveToNext());
        }
        c.close();
        db.close();
        return list;
    }

    public long blockUser(int currentUserId, int userIdToBlock) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_BLOCKER_ID, currentUserId);
        values.put(COL_BLOCKED_TARGET_ID, userIdToBlock);
        long id = db.insert(TABLE_BLOCKED, null, values);

        if (id != -1) {
            Log.d("DatabaseHelper", "User ID " + currentUserId + " successfully blocked User ID " + userIdToBlock);
        } else {
            Log.w("DatabaseHelper", "Block relationship already exists or insertion failed.");
        }
        db.close();
        return id;
    }

    public boolean unblockUser(int currentUserId, int userIdToUnblock) {
        SQLiteDatabase db = this.getWritableDatabase();

        String whereClause = COL_BLOCKER_ID + " = ? AND " + COL_BLOCKED_TARGET_ID + " = ?";
        String[] whereArgs = new String[]{String.valueOf(currentUserId), String.valueOf(userIdToUnblock)};

        int rowsDeleted = db.delete(TABLE_BLOCKED, whereClause, whereArgs);
        db.close();

        if (rowsDeleted > 0) {
            Log.d("DatabaseHelper", "User ID " + userIdToUnblock + " successfully unblocked by User ID " + currentUserId);
            return true;
        } else {
            return false;
        }
    }

    public List<User> getBlockedUsers(int currentUserId) {
        List<User> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String sql = "SELECT u.* FROM " + TABLE_USER + " u " +
                "INNER JOIN " + TABLE_BLOCKED + " b " +
                "ON u." + COL_USER_ID + " = b." + COL_BLOCKED_TARGET_ID +
                " WHERE b." + COL_BLOCKER_ID + "=" + currentUserId;

        Cursor c = db.rawQuery(sql, null);
        if (c.moveToFirst()) {
            do {
                User u = cursorToUser(c);
                list.add(u);
            } while (c.moveToNext());
        }
        c.close();
        db.close();
        return list;
    }

    public boolean isUserBlockedBy(int blockerId, int targetId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT 1 FROM " + TABLE_BLOCKED +
                " WHERE " + COL_BLOCKER_ID + " = ? AND " + COL_BLOCKED_TARGET_ID + " = ?";

        Cursor c = db.rawQuery(query, new String[]{String.valueOf(blockerId), String.valueOf(targetId)});

        boolean isBlocked = c.getCount() > 0;
        c.close();
        db.close();
        return isBlocked;
    }
    public LastMessageInfo getLastMessageForChat(int currentUserId, int friendId) {
        String lastMessageContent = "";
        String lastMessageTimestamp = "";
        boolean lastSentMessageIsRead = false;
        int unreadCount = 0;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        String queryLastMessage = "SELECT " +
                COL_MESSAGE_USER_ID + ", " +
                COL_MESSAGE_CONTENT + ", " +
                COL_MESSAGE_TIMESTAMP + ", " +
                COL_IS_READ + " " +
                "FROM " + TABLE_MESSAGE + " " +
                "WHERE (" +
                COL_MESSAGE_USER_ID + " = ? AND " + COL_MESSAGE_TO_ID + " = ?) " +
                "OR (" +
                COL_MESSAGE_USER_ID + " = ? AND " + COL_MESSAGE_TO_ID + " = ?) " +
                "ORDER BY " + COL_MESSAGE_TIMESTAMP + " DESC " +
                "LIMIT 1";

        String[] selectionArgs = new String[]{
                String.valueOf(currentUserId), String.valueOf(friendId),
                String.valueOf(friendId), String.valueOf(currentUserId)
        };

        try {
            cursor = db.rawQuery(queryLastMessage, selectionArgs);

            if (cursor.moveToFirst()) {
                @SuppressLint("Range") int senderId = cursor.getInt(cursor.getColumnIndex(COL_MESSAGE_USER_ID));
                @SuppressLint("Range") String content = cursor.getString(cursor.getColumnIndex(COL_MESSAGE_CONTENT));
                @SuppressLint("Range") String timestamp = cursor.getString(cursor.getColumnIndex(COL_MESSAGE_TIMESTAMP));
                @SuppressLint("Range") boolean isRead = cursor.getInt(cursor.getColumnIndex(COL_IS_READ)) == 1; // <-- NEW

                lastMessageContent = content;
                lastMessageTimestamp = timestamp;

                if (senderId == currentUserId) {
                    lastSentMessageIsRead = isRead;
                }
            }
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }

            String queryUnreadCount = "SELECT COUNT(*) FROM " + TABLE_MESSAGE +
                    " WHERE " + COL_MESSAGE_USER_ID + " = ? " +
                    " AND " + COL_MESSAGE_TO_ID + " = ? " +
                    " AND " + COL_IS_READ + " = 0";

            String[] unreadArgs = new String[]{String.valueOf(friendId), String.valueOf(currentUserId)};

            cursor = db.rawQuery(queryUnreadCount, unreadArgs);
            if (cursor.moveToFirst()) {
                unreadCount = cursor.getInt(0);
            }

        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error getting last message and count: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }

        return new LastMessageInfo(lastMessageContent, lastMessageTimestamp, unreadCount, lastSentMessageIsRead);
    }

    public boolean deleteConversation(int userA, int userB) {
        SQLiteDatabase db = this.getWritableDatabase();
        boolean success = false;
        db.beginTransaction();
        try {
            db.delete(TABLE_MESSAGE,
                    "(" + COL_MESSAGE_USER_ID + " = ? AND " + COL_MESSAGE_TO_ID + " = ?) OR " +
                            "(" + COL_MESSAGE_USER_ID + " = ? AND " + COL_MESSAGE_TO_ID + " = ?)",
                    new String[]{String.valueOf(userA), String.valueOf(userB),
                            String.valueOf(userB), String.valueOf(userA)});

            db.delete(TABLE_FRIEND,
                    "(" + COL_FRIEND_USER + " = ? AND " + COL_FRIEND_FRIEND + " = ?) OR " +
                            "(" + COL_FRIEND_USER + " = ? AND " + COL_FRIEND_FRIEND + " = ?)",
                    new String[]{String.valueOf(userA), String.valueOf(userB),
                            String.valueOf(userB), String.valueOf(userA)});

            db.setTransactionSuccessful();
            success = true;

        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error deleting conversation between " + userA + " and " + userB, e);
        } finally {
            db.endTransaction();
            db.close();
        }
        return success;
    }

    public List<User> getSentFriendRequests(int currentUserId) {
        List<User> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String sql = "SELECT u.* FROM " + TABLE_USER + " u INNER JOIN " + TABLE_FRIEND +
                " f ON u." + COL_USER_ID + " = f." + COL_FRIEND_FRIEND +
                " WHERE f." + COL_FRIEND_USER + " = ? AND f." + COL_FRIEND_STATUS + " = 'pending'";

        Cursor c = db.rawQuery(sql, new String[]{String.valueOf(currentUserId)});
        if (c.moveToFirst()) {
            do {
                User u = cursorToUser(c);
                list.add(u);
            } while (c.moveToNext());
        }
        c.close();
        db.close();
        return list;
    }

    public boolean removeFriendRequest(int senderId, int receiverId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsDeleted = db.delete(TABLE_FRIEND,
                COL_FRIEND_USER + " = ? AND " + COL_FRIEND_FRIEND + " = ? AND " + COL_FRIEND_STATUS + " = 'pending'",
                new String[]{String.valueOf(senderId), String.valueOf(receiverId)});

        db.close();
        return rowsDeleted > 0;
    }

    public boolean deleteUser(int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsAffected = 0;
        db.beginTransaction();
        try {
            db.delete(TABLE_MESSAGE, COL_MESSAGE_USER_ID + " = ? OR " + COL_MESSAGE_TO_ID + " = ?",
                    new String[]{String.valueOf(userId), String.valueOf(userId)});

            db.delete(TABLE_FRIEND, COL_FRIEND_USER + " = ? OR " + COL_FRIEND_FRIEND + " = ?",
                    new String[]{String.valueOf(userId), String.valueOf(userId)});

            db.delete(TABLE_BLOCKED, COL_BLOCKER_ID + " = ? OR " + COL_BLOCKED_TARGET_ID + " = ?",
                    new String[]{String.valueOf(userId), String.valueOf(userId)});

            rowsAffected = db.delete(TABLE_USER, COL_USER_ID + " = ?", new String[]{String.valueOf(userId)});

            if (rowsAffected > 0) {
                db.setTransactionSuccessful();
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error deleting user ID " + userId, e);
        } finally {
            db.endTransaction();
            db.close();
        }
        return rowsAffected > 0;
    }

    public List<Message> getUnreadMessagesForUser(int currentUserId) {
        List<Message> unreadMessages = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            String SELECT_QUERY = "SELECT * FROM " + TABLE_MESSAGE +
                    " WHERE " + COL_MESSAGE_TO_ID + " = ?" +
                    " AND " + COL_IS_READ + " = 0" +
                    " ORDER BY " + COL_MESSAGE_TIMESTAMP + " ASC";

            cursor = db.rawQuery(SELECT_QUERY, new String[]{String.valueOf(currentUserId)});

            if (cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_MESSAGE_ID));
                    int fromUserId = cursor.getInt(cursor.getColumnIndexOrThrow(COL_MESSAGE_USER_ID));
                    int toUserId = cursor.getInt(cursor.getColumnIndexOrThrow(COL_MESSAGE_TO_ID));
                    String content = cursor.getString(cursor.getColumnIndexOrThrow(COL_MESSAGE_CONTENT));
                    String timestamp = cursor.getString(cursor.getColumnIndexOrThrow(COL_MESSAGE_TIMESTAMP));
                    // isRead est 0 pour non lu.
                    boolean isRead = (cursor.getInt(cursor.getColumnIndexOrThrow(COL_IS_READ)) == 1);

                    unreadMessages.add(new Message(id, fromUserId, toUserId, content, timestamp, isRead));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error getting unread messages", e);
        } finally {
            if (cursor != null) cursor.close();
        }

        return unreadMessages;
    }


    public User getUser(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        User user = null;

        try {
            String SELECT_QUERY = "SELECT * FROM " + TABLE_USER +
                    " WHERE " + COL_USER_ID + " = ?";

            cursor = db.rawQuery(SELECT_QUERY, new String[]{String.valueOf(userId)});

            if (cursor.moveToFirst()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_USER_ID));
                String username = cursor.getString(cursor.getColumnIndexOrThrow(COL_USERNAME));
                String email = cursor.getString(cursor.getColumnIndexOrThrow(COL_EMAIL));
                String password = cursor.getString(cursor.getColumnIndexOrThrow(COL_PASSWORD));

                boolean isActive = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ISACTIVE)) == 1;

                String codeVerif = cursor.getString(cursor.getColumnIndexOrThrow(COL_CODEVERIF));
                String profileImagePath = cursor.getString(cursor.getColumnIndexOrThrow(COL_PROFILE_IMAGE));

                boolean isBlocked = cursor.getInt(cursor.getColumnIndexOrThrow(COL_IS_BLOCKED)) == 1; // Nécéssaire pour le constructeur

                // ⭐ UTILISATION DU CONSTRUCTEUR À 8 ARGUMENTS (CORRIGÉ) ⭐
                user = new User(id, username, email, password, isActive, codeVerif, profileImagePath, isBlocked);
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error getting user details with 8 fields", e);
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }

        return user;
    }

    // =========================================================================
    // ⭐ FIN LOGIQUE NOTIFICATION ⭐
    // =========================================================================

    public int setUserBlocked(int userId, boolean blocked) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        // Utilise la constante COL_IS_BLOCKED (qui est "is_blocked")
        cv.put(COL_IS_BLOCKED, blocked ? 1 : 0);

        int rows = db.update(TABLE_USER, cv, COL_USER_ID + "=?", new String[]{String.valueOf(userId)});
        db.close();
        return rows;
    }
}