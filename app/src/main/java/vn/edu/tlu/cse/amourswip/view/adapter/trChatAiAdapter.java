package vn.edu.tlu.cse.amourswip.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import vn.edu.tlu.cse.amourswip.R;
import vn.edu.tlu.cse.amourswip.model.data.trMessageAI;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class trChatAiAdapter extends RecyclerView.Adapter<trChatAiAdapter.MessageViewHolder> {

    private static final int VIEW_TYPE_USER = 1;
    private static final int VIEW_TYPE_AI = 2;

    private final List<trMessageAI> messageList;
    private long lastMessageTimestamp = -1;

    public trChatAiAdapter(List<trMessageAI> messageList) {
        this.messageList = messageList;
    }

    @Override
    public int getItemViewType(int position) {
        return messageList.get(position).isUserMessage() ? VIEW_TYPE_USER : VIEW_TYPE_AI;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_USER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_sent, parent, false);
            return new MessageViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_received, parent, false);
            return new MessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        trMessageAI message = messageList.get(position);
        holder.messageText.setText(message.getText());

        // Xử lý hiển thị ảnh đại diện
        if (message.isUserMessage()) {
            Glide.with(holder.itemView.getContext())
                    .load(R.drawable.pikachuuu) // Ảnh đại diện của người dùng
                    .into(holder.messageImage);
            if (holder.messageStatus != null) {
                holder.messageStatus.setText("Đã gửi");
                holder.messageStatus.setVisibility(View.VISIBLE);
            }
        } else {
            Glide.with(holder.itemView.getContext())
                    .load(R.drawable.ic_chatbot) // Ảnh đại diện của AI
                    .into(holder.messageImage);
            if (holder.messageStatus != null) {
                holder.messageStatus.setVisibility(View.GONE);
            }
        }

        // Xử lý hiển thị ngày và thời gian
        long currentTimestamp = message.getTimestamp();
        if (position == 0 || shouldShowDateTime(currentTimestamp, lastMessageTimestamp)) {
            if (holder.dateTime != null) {
                holder.dateTime.setVisibility(View.VISIBLE);
                holder.dateTime.setText(formatDateTime(currentTimestamp));
            }
        } else {
            if (holder.dateTime != null) {
                holder.dateTime.setVisibility(View.GONE);
            }
        }
        lastMessageTimestamp = currentTimestamp;
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    private boolean shouldShowDateTime(long currentTimestamp, long lastTimestamp) {
        if (lastTimestamp == -1) return true;
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String currentDate = dateFormat.format(new Date(currentTimestamp));
        String lastDate = dateFormat.format(new Date(lastTimestamp));
        return !currentDate.equals(lastDate);
    }

    private String formatDateTime(long timestamp) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, dd 'thg' MM, yyyy, HH:mm", Locale.getDefault());
        return dateFormat.format(new Date(timestamp));
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;
        ImageView messageImage;
        TextView dateTime;
        TextView messageStatus;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.message_text);
            messageImage = itemView.findViewById(R.id.message_image);
            dateTime = itemView.findViewById(R.id.date_time);
            messageStatus = itemView.findViewById(R.id.message_status);
        }
    }
}