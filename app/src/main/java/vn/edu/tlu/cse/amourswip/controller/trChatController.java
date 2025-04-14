package vn.edu.tlu.cse.amourswip.controller;

import androidx.annotation.NonNull;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.HashMap;
import java.util.Map;
import vn.edu.tlu.cse.amourswip.model.data.trMessageUser;
import vn.edu.tlu.cse.amourswip.model.data.xUser;
import vn.edu.tlu.cse.amourswip.model.repository.trChatRepository;
import vn.edu.tlu.cse.amourswip.view.fragment.trChatUserFragment;

public class trChatController {

    private final trChatUserFragment fragment;
    private final trChatRepository chatRepository;
    private final String friendId;
    private String currentUserImage;

    public trChatController(trChatUserFragment fragment, String friendId) {
        this.fragment = fragment;
        this.friendId = friendId;
        this.chatRepository = new trChatRepository(friendId);
        loadCurrentUserImage();
    }

    private void loadCurrentUserImage() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUserId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    xUser user = snapshot.getValue(xUser.class);
                    if (user != null && user.getPhotos() != null && !user.getPhotos().isEmpty()) {
                        currentUserImage = user.getPhotos().get(0);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                fragment.showError("Lỗi khi tải ảnh người dùng: " + error.getMessage());
            }
        });
    }

    public void loadFriendInfo() {
        chatRepository.getUserInfo(new trChatRepository.OnResultListener() {
            @Override
            public void onSuccess(xUser user) {
                fragment.updateUserInfo(user);
            }

            @Override
            public void onError(String error) {
                fragment.showError(error);
            }

            @Override
            public void onLoading() {
                // Hiển thị loading indicator nếu cần
            }
        });
    }

    public void onBackClicked() {
        fragment.getNavController().navigateUp();
    }

    public void onUserImageClicked() {
        fragment.startProfileMyFriendActivity();
    }

    public void onVideoCallClicked() {
        fragment.showError("Chức năng video call chưa được triển khai");
    }

    public void onMenuClicked() {
        fragment.showError("Chức năng menu chưa được triển khai");
    }

    public void onGifClicked() {
        fragment.showError("Chức năng gửi GIF chưa được triển khai");
    }

    public void onSendMessage(String messageText) {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String chatId = generateChatId(currentUserId, friendId);

        // Lưu tin nhắn vào node chats/{chatId}/messages
        DatabaseReference messagesRef = FirebaseDatabase.getInstance().getReference("chats").child(chatId).child("messages");
        String messageId = messagesRef.push().getKey();
        if (messageId == null) {
            fragment.showError("Lỗi khi tạo ID tin nhắn");
            return;
        }

        long timestamp = System.currentTimeMillis();
        trMessageUser message = new trMessageUser(messageId, currentUserId, messageText, timestamp, currentUserImage, "sent");

        Map<String, Object> messageValues = new HashMap<>();
        messageValues.put("messageId", message.getMessageId());
        messageValues.put("senderId", message.getSenderId());
        messageValues.put("message", message.getMessage());
        messageValues.put("timestamp", message.getTimestamp());
        messageValues.put("senderImage", message.getSenderImage());
        messageValues.put("status", message.getStatus());

        messagesRef.child(messageId).setValue(messageValues)
                .addOnSuccessListener(aVoid -> {
                    // Lưu tin nhắn cuối cùng vào node chats/{chatId}/lastMessage
                    DatabaseReference lastMessageRef = FirebaseDatabase.getInstance().getReference("chats").child(chatId).child("lastMessage");
                    Map<String, Object> lastMessageValues = new HashMap<>();
                    lastMessageValues.put("message", messageText);
                    lastMessageValues.put("timestamp", timestamp);
                    lastMessageValues.put("isUnread", false); // Đặt isUnread = false vì bạn đã gửi tin nhắn
                    lastMessageValues.put("senderId", currentUserId);
                    lastMessageRef.setValue(lastMessageValues)
                            .addOnFailureListener(e -> {
                                fragment.showError("Lỗi khi lưu tin nhắn cuối cùng: " + e.getMessage());
                            });
                })
                .addOnFailureListener(e -> {
                    fragment.showError("Lỗi khi gửi tin nhắn: " + e.getMessage());
                });
    }

    private String generateChatId(String userId1, String userId2) {
        return userId1.compareTo(userId2) < 0 ? userId1 + "_" + userId2 : userId2 + "_" + userId1;
    }
}