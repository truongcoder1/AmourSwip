package vn.edu.tlu.cse.amourswip.view.fragment;

import android.os.Bundle;
import android.util.Log;
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
import com.google.firebase.auth.FirebaseAuth;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import vn.edu.tlu.cse.amourswip.BuildConfig;
import vn.edu.tlu.cse.amourswip.R;
import vn.edu.tlu.cse.amourswip.model.data.trMessageAI;
import vn.edu.tlu.cse.amourswip.view.adapter.trChatAiAdapter;

public class trChatAiFragment extends Fragment {

    private static final String TAG = "ChatAIFragment";
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent";
    private static final int MAX_MESSAGES = 100; // Giới hạn số lượng tin nhắn

    private TextView title;
    private EditText messageInput;
    private ImageButton sendButton;
    private ImageView backArrow;
    private RecyclerView recyclerView;
    private List<trMessageAI> messageList;
    private trChatAiAdapter chatAdapter;
    private NavController navController;
    private OkHttpClient client;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        messageList = new ArrayList<>();
        client = new OkHttpClient();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_chat_ai, container, false);

        initializeViews(view);
        setupRecyclerView();
        setupNavigation();

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(getActivity(), "Vui lòng đăng nhập để trò chuyện", Toast.LENGTH_SHORT).show();
            navController.navigate(R.id.action_chatAiFragment_to_listChatFragment);
            return view;
        }

        displayWelcomeMessage();
        setupListeners();

        return view;
    }

    private void initializeViews(View view) {
        backArrow = view.findViewById(R.id.back_arrow);
        title = view.findViewById(R.id.title);
        recyclerView = view.findViewById(R.id.chat_recycler_view);
        messageInput = view.findViewById(R.id.message_input);
        sendButton = view.findViewById(R.id.send_button);
        title.setText("LOVE AI");
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        chatAdapter = new trChatAiAdapter(messageList);
        recyclerView.setAdapter(chatAdapter);
    }

    private void setupNavigation() {
        try {
            navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
        } catch (IllegalStateException e) {
            Log.e(TAG, "Navigation controller initialization failed", e);
            Toast.makeText(getActivity(), "Lỗi khởi tạo điều hướng", Toast.LENGTH_SHORT).show();
        }
    }

    private void displayWelcomeMessage() {
        if (messageList.isEmpty()) {
            Log.d(TAG, "Hiển thị tin nhắn chào mừng");
            trMessageAI welcomeMessage = new trMessageAI("Xin chào! Tôi là LOVE AI, tôi có thể giúp gì cho bạn hôm nay?", false, System.currentTimeMillis());
            addMessage(welcomeMessage);
        }
    }

    private void setupListeners() {
        sendButton.setOnClickListener(v -> sendMessage(messageInput.getText().toString()));
        backArrow.setOnClickListener(v -> {
            if (navController != null) navController.navigateUp();
        });
    }

    private void sendMessage(String messageText) {
        if (messageText.trim().isEmpty()) {
            Toast.makeText(getActivity(), "Vui lòng nhập tin nhắn", Toast.LENGTH_SHORT).show();
            return;
        }

        trMessageAI message = new trMessageAI(messageText, true, System.currentTimeMillis());
        addMessage(message);
        fetchAIResponse(messageText);
        messageInput.setText("");
    }

    private void addMessage(trMessageAI message) {
        if (messageList.size() >= MAX_MESSAGES) {
            messageList.remove(0); // Xóa tin nhắn cũ nhất nếu vượt quá giới hạn
            chatAdapter.notifyItemRemoved(0);
        }
        messageList.add(message);
        chatAdapter.notifyItemInserted(messageList.size() - 1);
        recyclerView.scrollToPosition(messageList.size() - 1);
    }

    private void fetchAIResponse(String userMessage) {
        if (!isAdded()) return;

        // Xây dựng ngữ cảnh hội thoại cho Gemini
        JSONArray messagesArray = new JSONArray();
        int maxContextMessages = 6;
        int startIndex = Math.max(0, messageList.size() - maxContextMessages);
        for (int i = startIndex; i < messageList.size(); i++) {
            trMessageAI msg = messageList.get(i);
            JSONObject messageObject = new JSONObject();
            try {
                messageObject.put("role", msg.isUserMessage() ? "user" : "model");
                JSONArray partsArray = new JSONArray();
                JSONObject partObject = new JSONObject();
                partObject.put("text", msg.getText());
                partsArray.put(partObject);
                messageObject.put("parts", partsArray);
                messagesArray.put(messageObject);
            } catch (Exception e) {
                Log.e(TAG, "Error building conversation context", e);
            }
        }

        // Thêm tin nhắn người dùng hiện tại
        JSONObject userMessageObject = new JSONObject();
        try {
            userMessageObject.put("role", "user");
            JSONArray partsArray = new JSONArray();
            JSONObject partObject = new JSONObject();
            partObject.put("text", userMessage);
            partsArray.put(partObject);
            userMessageObject.put("parts", partsArray);
            messagesArray.put(userMessageObject);
        } catch (Exception e) {
            Log.e(TAG, "Error adding user message to context", e);
        }

        // Tạo payload cho Gemini API
        JSONObject jsonPayload = new JSONObject();
        try {
            jsonPayload.put("contents", messagesArray);
            jsonPayload.put("generationConfig", new JSONObject()
                    .put("maxOutputTokens", 250)
                    .put("topP", 0.9)
                    .put("temperature", 0.7));
        } catch (Exception e) {
            Log.e(TAG, "Error creating API request", e);
            showError("Lỗi khi tạo yêu cầu API");
            return;
        }

        // Tạo yêu cầu API
        RequestBody body = RequestBody.create(jsonPayload.toString(), MediaType.parse("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(GEMINI_API_URL + "?key=" + BuildConfig.GEMINI_API_KEY)
                .post(body)
                .build();

        // Gửi yêu cầu bất đồng bộ
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> showError("Lỗi kết nối API: " + e.getMessage()));
                }
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!isAdded()) return;

                if (!response.isSuccessful()) {
                    String errorDetail = response.body() != null ? response.body().string() : "Không có chi tiết";
                    requireActivity().runOnUiThread(() -> showError("Lỗi API " + response.code() + ": " + errorDetail));
                    return;
                }

                String responseBody = response.body() != null ? response.body().string() : null;
                if (responseBody == null) {
                    requireActivity().runOnUiThread(() -> showError("Không nhận được dữ liệu từ API"));
                    return;
                }

                Log.d(TAG, "Raw API response: " + responseBody);

                try {
                    JSONObject jsonResponse = new JSONObject(responseBody);
                    JSONArray candidates = jsonResponse.getJSONArray("candidates");
                    if (candidates.length() > 0) {
                        JSONObject candidate = candidates.getJSONObject(0);
                        JSONObject content = candidate.getJSONObject("content");
                        JSONArray parts = content.getJSONArray("parts");
                        if (parts.length() > 0) {
                            String aiResponse = parts.getJSONObject(0).getString("text").trim();
                            Log.d(TAG, "Generated text: " + aiResponse);

                            if (!aiResponse.isEmpty()) {
                                requireActivity().runOnUiThread(() -> {
                                    trMessageAI aiMessage = new trMessageAI(aiResponse, false, System.currentTimeMillis());
                                    addMessage(aiMessage);
                                });
                            } else {
                                requireActivity().runOnUiThread(() -> showError("Phản hồi từ AI trống"));
                            }
                        } else {
                            requireActivity().runOnUiThread(() -> showError("Không có nội dung trong phản hồi"));
                        }
                    } else {
                        requireActivity().runOnUiThread(() -> showError("Không có phản hồi từ API"));
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing API response", e);
                    requireActivity().runOnUiThread(() -> showError("Lỗi phân tích phản hồi: " + e.getMessage()));
                }
            }
        });
    }

    private void showError(String error) {
        if (isAdded()) {
            Toast.makeText(getActivity(), error, Toast.LENGTH_LONG).show();
        }
    }
}