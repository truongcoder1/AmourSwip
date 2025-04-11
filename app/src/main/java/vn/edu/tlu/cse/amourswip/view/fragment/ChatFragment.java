package vn.edu.tlu.cse.amourswip.view.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import vn.edu.tlu.cse.amourswip.R;
import vn.edu.tlu.cse.amourswip.controller.ChatController;
import vn.edu.tlu.cse.amourswip.model.data.Message;
import vn.edu.tlu.cse.amourswip.model.data.User;
import vn.edu.tlu.cse.amourswip.view.activity.ProfileMyFriendActivity;
import vn.edu.tlu.cse.amourswip.view.adapter.MessageAdapter;

import java.util.ArrayList;
import java.util.List;

public class ChatFragment extends Fragment {

    private ImageButton backButton;
    private ImageView userImage;
    private TextView userName;
    private TextView userStatus;
    private ImageView statusIcon;
    private ImageButton videoCallButton;
    private ImageButton menuButton;
    private RecyclerView messagesRecyclerView;
    private EditText messageInput;
    private ImageButton gifButton;
    private ImageButton sendButton;
    private ChatController controller;
    private String friendId;
    private String chatId;
    private String currentUserId;
    private NavController navController;
    private MessageAdapter messageAdapter;
    private List<Message> messageList;
    private DatabaseReference messagesRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_chat, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = Navigation.findNavController(view);

        backButton = view.findViewById(R.id.back_button);
        userImage = view.findViewById(R.id.user_image);
        userName = view.findViewById(R.id.user_name);
        userStatus = view.findViewById(R.id.user_status);
        statusIcon = view.findViewById(R.id.status_icon);
        videoCallButton = view.findViewById(R.id.video_call_button);
        menuButton = view.findViewById(R.id.menu_button);
        messagesRecyclerView = view.findViewById(R.id.messages_recycler_view);
        messageInput = view.findViewById(R.id.message_input);
        gifButton = view.findViewById(R.id.gif_button);
        sendButton = view.findViewById(R.id.send_button);

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (getArguments() != null) {
            friendId = getArguments().getString("userId");
            String name = getArguments().getString("userName");
            boolean isOnline = getArguments().getBoolean("isOnline");

            userName.setText(name != null ? name : "N/A");
            userStatus.setText(isOnline ? "Online" : "Offline");
            statusIcon.setVisibility(isOnline ? View.VISIBLE : View.GONE);
        }

        if (friendId == null) {
            Toast.makeText(getContext(), "Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show();
            navController.popBackStack();
            return;
        }

        // Tạo chatId dựa trên userId và friendId
        chatId = currentUserId.compareTo(friendId) < 0 ? currentUserId + "_" + friendId : friendId + "_" + currentUserId;
        messagesRef = FirebaseDatabase.getInstance().getReference("chats").child(chatId).child("messages");

        // Khởi tạo RecyclerView và MessageAdapter
        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(messageList, currentUserId);
        messagesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        messagesRecyclerView.setAdapter(messageAdapter);

        // Lắng nghe tin nhắn theo thời gian thực
        listenForMessages();

        // Khởi tạo Controller
        controller = new ChatController(this, friendId);
        controller.loadFriendInfo();

        // Xử lý các sự kiện click
        backButton.setOnClickListener(v -> controller.onBackClicked());
        userImage.setOnClickListener(v -> controller.onUserImageClicked());
        videoCallButton.setOnClickListener(v -> controller.onVideoCallClicked());
        menuButton.setOnClickListener(v -> controller.onMenuClicked());
        gifButton.setOnClickListener(v -> controller.onGifClicked());
        sendButton.setOnClickListener(v -> {
            String message = messageInput.getText().toString().trim();
            if (!message.isEmpty()) {
                controller.onSendMessage(message);
                messageInput.setText("");
            } else {
                Toast.makeText(getContext(), "Vui lòng nhập tin nhắn", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void listenForMessages() {
        messagesRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Message message = snapshot.getValue(Message.class);
                if (message != null) {
                    messageAdapter.addMessage(message);
                    messagesRecyclerView.scrollToPosition(messageList.size() - 1); // Cuộn xuống tin nhắn mới nhất
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                // Xử lý nếu tin nhắn bị chỉnh sửa (nếu cần)
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                // Xử lý nếu tin nhắn bị xóa (nếu cần)
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                // Xử lý nếu tin nhắn thay đổi vị trí (nếu cần)
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Lỗi khi tải tin nhắn: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Phương thức để Controller gọi để cập nhật giao diện
    public void updateUserInfo(User user) {
        if (user != null) {
            userName.setText(user.getName() != null ? user.getName() : "N/A");
            userStatus.setText(user.isOnline() ? "Online" : "Offline");
            statusIcon.setVisibility(user.isOnline() ? View.VISIBLE : View.GONE);

            if (user.getPhotos() != null && !user.getPhotos().isEmpty()) {
                String avatarUrl = user.getPhotos().get(0);
                if (avatarUrl != null) {
                    Glide.with(getContext())
                            .load(avatarUrl)
                            .placeholder(R.drawable.gai1)
                            .error(R.drawable.gai1)
                            .into(userImage);
                }
            } else {
                Glide.with(getContext())
                        .load(R.drawable.gai1)
                        .into(userImage);
            }
        }
    }

    public void showError(String error) {
        Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
    }

    public NavController getNavController() {
        return navController;
    }

    public void startProfileMyFriendActivity() {
        Intent intent = new Intent(getActivity(), ProfileMyFriendActivity.class);
        intent.putExtra("friendId", friendId);
        startActivity(intent);
    }
}