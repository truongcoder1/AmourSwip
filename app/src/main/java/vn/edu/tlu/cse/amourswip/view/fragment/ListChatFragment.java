package vn.edu.tlu.cse.amourswip.view.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import java.util.ArrayList;
import java.util.List;
import vn.edu.tlu.cse.amourswip.R;
import vn.edu.tlu.cse.amourswip.view.adapter.NotificationAdapter;
import vn.edu.tlu.cse.amourswip.controller.ListChatController;
import vn.edu.tlu.cse.amourswip.model.data.Notification;

public class ListChatFragment extends Fragment {

    private RecyclerView notificationsRecyclerView;
    private ImageButton chatbotButton;
    private List<Notification> notificationList;
    private NotificationAdapter adapter;
    private FirebaseAuth auth;
    private NavController navController;
    private ListChatController controller;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_listwatingchat, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = Navigation.findNavController(view);

        auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() == null) {
            Toast.makeText(getActivity(), "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        notificationsRecyclerView = view.findViewById(R.id.notifications_recycler_view);
        chatbotButton = view.findViewById(R.id.chatbot_button);

        notificationList = new ArrayList<>();
        adapter = new NotificationAdapter(notificationList, this::onNotificationClicked);
        notificationsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        notificationsRecyclerView.setAdapter(adapter);

        chatbotButton.setOnClickListener(v -> {
            Toast.makeText(getActivity(), "Mở chatbot", Toast.LENGTH_SHORT).show();
        });

        // Kiểm tra dữ liệu từ Bundle
        Bundle args = getArguments();
        if (args != null && args.containsKey("chatId")) {
            String chatId = args.getString("chatId");
            // Không hiển thị Toast, có thể sử dụng chatId để làm nổi bật thông báo nếu cần
        }

        // Khởi tạo Controller
        controller = new ListChatController(this);
        controller.loadNotifications();
    }

    private void onNotificationClicked(Notification notification) {
        controller.onNotificationClicked(notification);
    }

    // Phương thức để Controller gọi để cập nhật giao diện
    public void updateNotifications(List<Notification> notifications) {
        notificationList.clear();
        notificationList.addAll(notifications);
        adapter.notifyDataSetChanged();
    }

    public void showError(String error) {
        Toast.makeText(getActivity(), error, Toast.LENGTH_LONG).show();
    }

    public NavController getNavController() {
        return navController;
    }
}