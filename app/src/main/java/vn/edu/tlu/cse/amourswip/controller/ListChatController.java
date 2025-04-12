package vn.edu.tlu.cse.amourswip.controller;

import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import vn.edu.tlu.cse.amourswip.R;
import vn.edu.tlu.cse.amourswip.model.data.Notification;
import vn.edu.tlu.cse.amourswip.model.repository.NotificationRepository;
import vn.edu.tlu.cse.amourswip.view.fragment.ListChatFragment;

public class ListChatController {

    private final ListChatFragment fragment;
    private final NotificationRepository notificationRepository;
    private final String currentUserId;

    public ListChatController(ListChatFragment fragment) {
        this.fragment = fragment;
        this.notificationRepository = new NotificationRepository();
        this.currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    public void loadNotifications() {
        notificationRepository.getNotifications(new NotificationRepository.OnResultListener() {
            @Override
            public void onSuccess(List<Notification> notifications) {
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

    public void onNotificationClicked(Notification notification) {
        // Tạo chatId
        String chatId = currentUserId.compareTo(notification.getUserId()) < 0
                ? currentUserId + "_" + notification.getUserId()
                : notification.getUserId() + "_" + currentUserId;

        // Cập nhật trạng thái isUnread thành false trong Firebase
        DatabaseReference lastMessageRef = FirebaseDatabase.getInstance().getReference("chats").child(chatId).child("lastMessage");
        Map<String, Object> updates = new HashMap<>();
        updates.put("isUnread", false);
        lastMessageRef.updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    // Đánh dấu thông báo là đã xem trong giao diện
                    notification.setUnread(false);
                    // Điều hướng đến ChatFragment
                    Bundle bundle = new Bundle();
                    bundle.putString("userId", notification.getUserId());
                    bundle.putString("userName", notification.getUserName());
                    fragment.getNavController().navigate(R.id.action_listChatFragment_to_chatFragment, bundle);
                })
                .addOnFailureListener(e -> {
                    fragment.showError("Lỗi khi cập nhật trạng thái đọc: " + e.getMessage());
                });
    }
}