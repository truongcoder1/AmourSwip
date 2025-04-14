package vn.edu.tlu.cse.amourswip.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;
import vn.edu.tlu.cse.amourswip.R;
import vn.edu.tlu.cse.amourswip.model.data.chNotification;

public class chNotificationAdapter extends RecyclerView.Adapter<chNotificationAdapter.ViewHolder> {

    private List<chNotification> notificationList;
    private OnItemClickListener listener;

    public chNotificationAdapter(List<chNotification> notificationList, OnItemClickListener listener) {
        this.notificationList = notificationList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_listwaitingchat, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        chNotification notification = notificationList.get(position);

        // Gán dữ liệu
        holder.userName.setText(notification.getUserName());
        holder.lastMessage.setText(notification.getLastMessage());
        holder.notificationTime.setText(notification.getTime());

        // Cập nhật trạng thái xem (chỉ hiển thị icon đỏ nếu chưa đọc)
        if (notification.isUnread()) {
            holder.unreadIcon.setVisibility(View.VISIBLE);
        } else {
            holder.unreadIcon.setVisibility(View.GONE);
        }

        // Tải hình ảnh người dùng với Glide
        Glide.with(holder.itemView.getContext())
                .load(notification.getUserImage())
                .circleCrop()
                .placeholder(R.drawable.gai1)
                .error(R.drawable.gai1)
                .into(holder.userImage);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(notification);
            }
        });
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    public interface OnItemClickListener {
        void onItemClick(chNotification notification);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView userImage;
        TextView userName;
        TextView lastMessage;
        TextView notificationTime;
        ImageView unreadIcon;

        ViewHolder(View itemView) {
            super(itemView);
            userImage = itemView.findViewById(R.id.user_image);
            userName = itemView.findViewById(R.id.user_name);
            lastMessage = itemView.findViewById(R.id.last_message);
            notificationTime = itemView.findViewById(R.id.notification_time);
            unreadIcon = itemView.findViewById(R.id.unread_icon);
        }
    }
}