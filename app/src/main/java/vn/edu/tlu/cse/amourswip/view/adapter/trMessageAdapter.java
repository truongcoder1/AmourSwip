package vn.edu.tlu.cse.amourswip.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import vn.edu.tlu.cse.amourswip.R;
import vn.edu.tlu.cse.amourswip.model.data.trMessageUser;

public class trMessageAdapter extends RecyclerView.Adapter<trMessageAdapter.MessageViewHolder> {

    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;

    private List<trMessageUser> messageList;
    private String currentUserId;

    public trMessageAdapter(List<trMessageUser> messageList, String currentUserId) {
        this.messageList = messageList;
        this.currentUserId = currentUserId;
    }

    public void addMessage(trMessageUser message) {
        messageList.add(message);
        notifyItemInserted(messageList.size() - 1);// khiến recy hiển thị tn mới mà ko cần làm mới toàn bộ danh sách
    }

    @Override
    public int getItemViewType(int position) {
        trMessageUser message = messageList.get(position);
        return message.getSenderId().equals(currentUserId) ? VIEW_TYPE_SENT : VIEW_TYPE_RECEIVED;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == VIEW_TYPE_SENT) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_sent, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_received, parent, false);
        }
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        trMessageUser message = messageList.get(position);
        holder.messageText.setText(message.getMessage());

        // Hiển thị ảnh người gửi nếu có
        if (message.getSenderImage() != null && !message.getSenderImage().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(message.getSenderImage())
                    .circleCrop()
                    .placeholder(R.drawable.gai1)
                    .error(R.drawable.gai1)
                    .into(holder.senderImage);
        } else {
            holder.senderImage.setImageResource(R.drawable.gai1);
        }

        // Hiển thị ngày và thời gian
        if (shouldShowDateTime(position)) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy, HH:mm", Locale.getDefault());
            String dateTime = dateFormat.format(new Date(message.getTimestamp()));
            holder.dateTime.setText(dateTime);
            holder.dateTime.setVisibility(View.VISIBLE);
        } else {
            holder.dateTime.setVisibility(View.GONE);
        }

        // Hiển thị trạng thái "Đã gửi"/"Đã xem" cho tin nhắn gửi
        if (getItemViewType(position) == VIEW_TYPE_SENT) {
            if (message.getStatus() != null) {
                if (message.getStatus().equals("sent")) {
                    holder.messageStatus.setText("Đã gửi");
                } else if (message.getStatus().equals("seen")) {
                    holder.messageStatus.setText("Đã xem");
                }
                holder.messageStatus.setVisibility(View.VISIBLE);
            } else {
                holder.messageStatus.setVisibility(View.GONE);
            }
        }
    }

    private boolean shouldShowDateTime(int position) {
        if (position == 0) {
            return true; // Luôn hiển thị ngày cho tin nhắn đầu tiên
        }
        trMessageUser currentMessage = messageList.get(position);
        trMessageUser previousMessage = messageList.get(position - 1);

        // So sánh ngày của tin nhắn hiện tại và tin nhắn trước đó
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        String currentDate = dateFormat.format(new Date(currentMessage.getTimestamp()));
        String previousDate = dateFormat.format(new Date(previousMessage.getTimestamp()));
        return !currentDate.equals(previousDate);
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;
        ImageView senderImage;
        TextView dateTime;
        TextView messageStatus;

        MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.message_text);
            senderImage = itemView.findViewById(R.id.message_image);
            dateTime = itemView.findViewById(R.id.date_time);
            messageStatus = itemView.findViewById(R.id.message_status);
        }
    }
}