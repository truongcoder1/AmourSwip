package vn.edu.tlu.cse.amourswip.controller;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import vn.edu.tlu.cse.amourswip.model.data.User;
import vn.edu.tlu.cse.amourswip.model.repository.ChatRepository;
import vn.edu.tlu.cse.amourswip.view.fragment.ChatFragment;

public class ChatController {

    private final ChatFragment fragment;
    private final ChatRepository chatRepository;
    private final String friendId;

    public ChatController(ChatFragment fragment, String friendId) {
        this.fragment = fragment;
        this.friendId = friendId;
        this.chatRepository = new ChatRepository(friendId);
    }

    public void loadFriendInfo() {
        chatRepository.getUserInfo(new ChatRepository.OnResultListener() {
            @Override
            public void onSuccess(User user) {
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

    public void onSendMessage(String message) {
        DatabaseReference messagesRef = FirebaseDatabase.getInstance().getReference("messages");
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String chatId = generateChatId(currentUserId, friendId);

        DatabaseReference messageRef = messagesRef.child(chatId).push();
        messageRef.child("senderId").setValue(currentUserId);
        messageRef.child("receiverId").setValue(friendId);
        messageRef.child("message").setValue(message);
        messageRef.child("timestamp").setValue(System.currentTimeMillis());
    }

    private String generateChatId(String userId1, String userId2) {
        return userId1.compareTo(userId2) < 0 ? userId1 + "_" + userId2 : userId2 + "_" + userId1;
    }
}