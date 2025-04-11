package vn.edu.tlu.cse.amourswip.controller;

import android.os.Bundle;

import java.util.List;

import vn.edu.tlu.cse.amourswip.R;
import vn.edu.tlu.cse.amourswip.model.data.Notification;
import vn.edu.tlu.cse.amourswip.model.repository.NotificationRepository;
import vn.edu.tlu.cse.amourswip.view.fragment.ListChatFragment;

public class ListChatController {

    private final ListChatFragment fragment;
    private final NotificationRepository notificationRepository;

    public ListChatController(ListChatFragment fragment) {
        this.fragment = fragment;
        this.notificationRepository = new NotificationRepository();
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
        Bundle bundle = new Bundle();
        bundle.putString("userId", notification.getUserId());
        bundle.putString("userName", notification.getUserName());
        bundle.putBoolean("isOnline", notification.isOnline());
        fragment.getNavController().navigate(R.id.action_listChatFragment_to_chatFragment, bundle);

        // Đánh dấu thông báo là đã xem
        notification.setUnread(false);
    }
}