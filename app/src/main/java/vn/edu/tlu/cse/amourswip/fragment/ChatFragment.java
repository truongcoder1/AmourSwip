package vn.edu.tlu.cse.amourswip.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

import vn.edu.tlu.cse.amourswip.R;
import vn.edu.tlu.cse.amourswip.adapter.MessageAdapter;
import vn.edu.tlu.cse.amourswip.datalayer.model.Message;

public class ChatFragment extends Fragment {

    private RecyclerView messagesRecyclerView;
    private EditText messageInput;
    private ImageButton sendButton;
    private ImageButton backButton;
    private TextView userName;

    private MessageAdapter messageAdapter;
    private List<Message> messages = new ArrayList<>();
    private DatabaseReference database;

    private String currentUserId;
    private String friendId;
    private String chatId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        // Khởi tạo các view bằng findViewById
        messagesRecyclerView = view.findViewById(R.id.messages_recycler_view);
        messageInput = view.findViewById(R.id.message_input);
        sendButton = view.findViewById(R.id.send_button);
        backButton = view.findViewById(R.id.back_button);
        userName = view.findViewById(R.id.user_name);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Lấy thông tin từ arguments
        Bundle args = getArguments();
        if (args != null) {
            friendId = args.getString("friendId", "");
            String friendName = args.getString("friendName", "");
            userName.setText(friendName);
        }

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Tạo chatId (kết hợp userId và friendId, đảm bảo thứ tự nhất quán)
        chatId = currentUserId.compareTo(friendId) < 0 ? currentUserId + "_" + friendId : friendId + "_" + currentUserId;

        // Khởi tạo Firebase Realtime Database
        database = FirebaseDatabase.getInstance().getReference().child("chats").child(chatId).child("messages");

        // Khởi tạo RecyclerView
        messageAdapter = new MessageAdapter(messages, currentUserId);
        messagesRecyclerView.setAdapter(messageAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setStackFromEnd(true); // Cuộn xuống dưới cùng
        messagesRecyclerView.setLayoutManager(layoutManager);

        // Lắng nghe tin nhắn từ Firebase
        listenForMessages();

        // Xử lý nút gửi tin nhắn
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String messageText = messageInput.getText().toString().trim();
                if (!messageText.isEmpty()) {
                    sendMessage(messageText);
                    messageInput.setText("");
                }
            }
        });

        // Xử lý nút quay lại
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavController navController = Navigation.findNavController(v);
                navController.navigateUp();
            }
        });
    }

    private void listenForMessages() {
        database.orderByChild("timestamp").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Message message = snapshot.getValue(Message.class);
                if (message != null) {
                    messages.add(message);
                    messageAdapter.notifyItemInserted(messages.size() - 1);
                    messagesRecyclerView.scrollToPosition(messages.size() - 1);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {}

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Lỗi: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendMessage(String messageText) {
        Message message = new Message(
                currentUserId,
                messageText,
                System.currentTimeMillis(),
                null
        );
        database.push().setValue(message);
    }
}