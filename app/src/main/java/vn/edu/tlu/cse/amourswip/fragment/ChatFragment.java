package vn.edu.tlu.cse.amourswip.fragment;

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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import vn.edu.tlu.cse.amourswip.R;
import vn.edu.tlu.cse.amourswip.activity.ProfileMyFriendActivity;

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
    private DatabaseReference userRef;
    private String friendId; // ID của người bạn đang chat

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        // Khởi tạo các view bằng findViewById
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

        // Lấy friendId từ arguments
        if (getArguments() != null) {
            friendId = getArguments().getString("friendId");
        }

        if (friendId == null) {
            Toast.makeText(getContext(), "Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show();
            return view;
        }

        // Khởi tạo Firebase
        userRef = FirebaseDatabase.getInstance().getReference().child("users").child(friendId);

        // Thiết lập RecyclerView
        messagesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        // Lấy và hiển thị thông tin người bạn
        loadFriendInfo();

        // Xử lý nút quay lại
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavController navController = Navigation.findNavController(v);
                navController.navigateUp();
            }
        });

        // Xử lý nhấn vào ảnh đại diện
        userImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ProfileMyFriendActivity.class);
                intent.putExtra("friendId", friendId);
                startActivity(intent);
            }
        });

        // Xử lý nút video call
        videoCallButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "Chức năng video call chưa được triển khai", Toast.LENGTH_SHORT).show();
            }
        });

        // Xử lý nút menu
        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "Chức năng menu chưa được triển khai", Toast.LENGTH_SHORT).show();
            }
        });

        // Xử lý nút gửi GIF
        gifButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "Chức năng gửi GIF chưa được triển khai", Toast.LENGTH_SHORT).show();
            }
        });

        // Xử lý nút gửi tin nhắn
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = messageInput.getText().toString().trim();
                if (!message.isEmpty()) {
                    sendMessage(message);
                    messageInput.setText("");
                }
            }
        });

        return view;
    }

    private void loadFriendInfo() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class);
                    String avatarUrl = snapshot.child("avatarUrl").getValue(String.class);
                    Boolean isOnline = snapshot.child("online").getValue(Boolean.class);

                    userName.setText(name != null ? name : "N/A");
                    userStatus.setText(isOnline != null && isOnline ? "Online" : "Offline");
                    statusIcon.setVisibility(isOnline != null && isOnline ? View.VISIBLE : View.GONE);

                    if (avatarUrl != null) {
                        Glide.with(getContext())
                                .load(avatarUrl)
                                .placeholder(R.drawable.gai1)
                                .error(R.drawable.gai1)
                                .into(userImage);
                    }
                } else {
                    Toast.makeText(getContext(), "Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Lỗi khi tải thông tin: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendMessage(String message) {
        // Giả định bạn có một node "messages" trong Firebase để lưu tin nhắn
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
        // Tạo chatId duy nhất bằng cách sắp xếp userId1 và userId2 theo thứ tự
        return userId1.compareTo(userId2) < 0 ? userId1 + "_" + userId2 : userId2 + "_" + userId1;
    }
}