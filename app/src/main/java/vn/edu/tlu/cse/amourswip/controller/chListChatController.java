package vn.edu.tlu.cse.amourswip.controller;

import android.os.Bundle;
import android.util.Log;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import vn.edu.tlu.cse.amourswip.R;
import vn.edu.tlu.cse.amourswip.model.data.chNotification;
import vn.edu.tlu.cse.amourswip.model.repository.chNotificationRepository;
import vn.edu.tlu.cse.amourswip.view.fragment.chListChatFragment;

public class chListChatController {

    private static final String TAG = "ListChatController";
    private final chListChatFragment fragment;
    private final chNotificationRepository notificationRepository;
    private final String currentUserId;

    public chListChatController(chListChatFragment fragment) {
        this.fragment = fragment;
        this.notificationRepository = new chNotificationRepository();
        this.currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    public void loadNotifications() {
        notificationRepository.getNotifications(new chNotificationRepository.OnResultListener() {
            @Override
            public void onSuccess(List<chNotification> notifications) {
                fragment.updateNotifications(notifications);
            }

            @Override
            public void onEmpty() {
                fragment.showError("Không có match nào để hiển thị");
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

    public void onNotificationClicked(chNotification notification) {
        String chatId = currentUserId.compareTo(notification.getUserId()) < 0
                ? currentUserId + "_" + notification.getUserId()
                : notification.getUserId() + "_" + currentUserId;

        DatabaseReference lastMessageRef = FirebaseDatabase.getInstance().getReference("chats").child(chatId).child("lastMessage");
        Map<String, Object> updates = new HashMap<>();
        updates.put("isUnread", false);
        lastMessageRef.updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    notification.setUnread(false);
                    Bundle bundle = new Bundle();
                    bundle.putString("userId", notification.getUserId());
                    bundle.putString("userName", notification.getUserName());
                    fragment.getNavController().navigate(R.id.action_listChatFragment_to_chatUserFragment, bundle);
                })
                .addOnFailureListener(e -> {
                    fragment.showError("Lỗi khi cập nhật trạng thái đọc: " + e.getMessage());
                });
    }

    public void onDestroy() {
        notificationRepository.removeListeners();
        Log.d(TAG, "onDestroy: Removed Firebase listeners");
    }
}