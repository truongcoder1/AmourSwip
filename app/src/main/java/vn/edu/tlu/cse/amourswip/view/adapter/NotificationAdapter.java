package vn.edu.tlu.cse.amourswip.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import vn.edu.tlu.cse.amourswip.R;
import vn.edu.tlu.cse.amourswip.model.data.Notification;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private List<Notification> notificationList;
    private OnItemClickListener listener;

    public NotificationAdapter(List<Notification> notificationList, OnItemClickListener listener) {
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
        Notification notification = notificationList.get(position);

        holder.userName.setText(notification.getUserName());
        holder.notificationStatus.setText(notification.getStatus());
        holder.notificationTime.setText(notification.getTime());

        // Cập nhật trạng thái online/offline
        if (notification.isOnline()) {
            holder.onlineStatus.setBackgroundResource(R.drawable.custom_online);
            holder.notificationStatus.setText("Online");
        } else {
            holder.onlineStatus.setBackgroundResource(R.drawable.custom_offline);
            holder.notificationStatus.setText("Offline");
        }

        // Cập nhật trạng thái xem (chấm đỏ hoặc "Đã xem")
        if (notification.isUnread()) {
            holder.unreadIcon.setVisibility(View.VISIBLE);
            holder.readStatus.setVisibility(View.GONE);
        } else {
            holder.unreadIcon.setVisibility(View.GONE);
            holder.readStatus.setVisibility(View.VISIBLE);
        }

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
        void onItemClick(Notification notification);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView userImage;
        TextView userName;
        TextView notificationStatus;
        TextView notificationTime;
        ImageView unreadIcon;
        TextView readStatus; // Thêm TextView cho "Đã xem"
        View onlineStatus;

        ViewHolder(View itemView) {
            super(itemView);
            userImage = itemView.findViewById(R.id.user_image);
            userName = itemView.findViewById(R.id.user_name);
            notificationStatus = itemView.findViewById(R.id.notification_status);
            notificationTime = itemView.findViewById(R.id.notification_time);
            unreadIcon = itemView.findViewById(R.id.unread_icon);
            readStatus = itemView.findViewById(R.id.read_status); // Khởi tạo TextView
            onlineStatus = itemView.findViewById(R.id.online_status);
        }
    }
}