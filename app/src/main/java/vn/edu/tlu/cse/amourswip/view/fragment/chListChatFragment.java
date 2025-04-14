package vn.edu.tlu.cse.amourswip.view.fragment;

import android.os.Bundle;
import android.util.Log;
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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.firebase.auth.FirebaseAuth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import vn.edu.tlu.cse.amourswip.R;
import vn.edu.tlu.cse.amourswip.view.adapter.chNotificationAdapter;
import vn.edu.tlu.cse.amourswip.controller.chListChatController;
import vn.edu.tlu.cse.amourswip.model.data.chNotification;

public class chListChatFragment extends Fragment {
    private RecyclerView notificationsRecyclerView;
    private ImageButton chatbotButton;
    private SwipeRefreshLayout swipeRefreshLayout; // Thêm SwipeRefreshLayout
    private List<chNotification> notificationList;
    private chNotificationAdapter adapter;
    private FirebaseAuth auth;
    private NavController navController;
    private chListChatController controller;

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
            if (getActivity() != null) {
                Toast.makeText(getActivity(), "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        notificationsRecyclerView = view.findViewById(R.id.notifications_recycler_view);
        chatbotButton = view.findViewById(R.id.chatbot_button);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout); // Khởi tạo SwipeRefreshLayout

        notificationList = new ArrayList<>();
        adapter = new chNotificationAdapter(notificationList, this::onNotificationClicked);
        notificationsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        notificationsRecyclerView.setAdapter(adapter);

        chatbotButton.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putBoolean("isChatWithAI", true);
            navController.navigate(R.id.action_listChatFragment_to_chatAIFragment, bundle);
        });

        // Thêm listener cho SwipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener(() -> {
            controller.loadNotifications(); // Gọi lại loadNotifications để reload danh sách
            swipeRefreshLayout.setRefreshing(false); // Tắt hiệu ứng làm mới
        });

        Bundle args = getArguments();
        if (args != null && args.containsKey("chatId")) {
            String chatId = args.getString("chatId");
        }

        controller = new chListChatController(this);
        controller.loadNotifications();
    }

    private void onNotificationClicked(chNotification notification) {
        controller.onNotificationClicked(notification);
    }

    public void updateNotifications(List<chNotification> notifications) {
        Log.d("ListChatFragment", "Updating notifications, size: " + notifications.size());
        for (chNotification n : notifications) {
            Log.d("ListChatFragment", "Notification: " + n.getUserName() + ", timestamp: " + n.getTimestamp());
        }
        notificationList.clear();
        // Sắp xếp theo timestamp giảm dần (mới nhất lên đầu)
        Collections.sort(notifications, new Comparator<chNotification>() {
            @Override
            public int compare(chNotification n1, chNotification n2) {
                return Long.compare(n2.getTimestamp(), n1.getTimestamp()); // Giảm dần
            }
        });
        notificationList.addAll(notifications);
        adapter.notifyDataSetChanged();
    }

    public void showError(String error) {
        if (getActivity() != null) {
            Toast.makeText(getActivity(), error, Toast.LENGTH_LONG).show();
        } else {
            Log.e("ListChatFragment", "Cannot show error toast, Activity is null: " + error);
        }
    }

    public NavController getNavController() {
        return navController;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (controller != null) {
            controller.onDestroy();
        }
    }
}