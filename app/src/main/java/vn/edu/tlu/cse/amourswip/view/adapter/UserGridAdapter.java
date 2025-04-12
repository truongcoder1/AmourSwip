package vn.edu.tlu.cse.amourswip.view.adapter;

import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.ArrayList;
import java.util.List;
import vn.edu.tlu.cse.amourswip.R;
import vn.edu.tlu.cse.amourswip.model.data.User;

public class UserGridAdapter extends RecyclerView.Adapter<UserGridAdapter.UserViewHolder> {

    private List<User> userList;
    private OnItemClickListener listener;
    private double currentLatitude;
    private double currentLongitude;

    public UserGridAdapter(List<User> userList, OnItemClickListener listener, double currentLatitude, double currentLongitude) {
        this.userList = userList;
        this.listener = listener;
        this.currentLatitude = currentLatitude;
        this.currentLongitude = currentLongitude;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_grid_photo, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);

        // Xử lý giá trị residence là null
        String residence = user.getResidence() != null ? user.getResidence() : "Không xác định";
        // Hiển thị tên, tuổi, nơi ở, và khoảng cách
        String distance = calculateDistance(user.getLatitude(), user.getLongitude());
        holder.userNameAgeDistance.setText(user.getName() + ", " + user.getAge() + ", " + residence + ", " + distance);

        // Tải hình ảnh người dùng
        if (user.getPhotos() != null && !user.getPhotos().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(user.getPhotos().get(0))
                    .placeholder(R.drawable.gai1)
                    .error(R.drawable.gai1)
                    .into(holder.gridPhoto);
        } else {
            holder.gridPhoto.setImageResource(R.drawable.gai1);
        }

        // Xử lý sự kiện click
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(user);
            }
        });
    }

    private String calculateDistance(double latitude, double longitude) {
        if (currentLatitude == 0 || currentLongitude == 0) {
            return "N/A";
        }

        Location currentLocation = new Location("");
        currentLocation.setLatitude(currentLatitude);
        currentLocation.setLongitude(currentLongitude);

        Location userLocation = new Location("");
        userLocation.setLatitude(latitude);
        userLocation.setLongitude(longitude);

        float distanceInMeters = currentLocation.distanceTo(userLocation);
        float distanceInKm = distanceInMeters / 1000;
        return String.format("%.1f km", distanceInKm);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public void updateList(List<User> newList) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return userList.size();
            }

            @Override
            public int getNewListSize() {
                return newList.size();
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                return userList.get(oldItemPosition).getUid().equals(newList.get(newItemPosition).getUid());
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                return userList.get(oldItemPosition).equals(newList.get(newItemPosition));
            }
        });
        userList = new ArrayList<>(newList);
        diffResult.dispatchUpdatesTo(this);
    }

    public interface OnItemClickListener {
        void onItemClick(User user);
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        ImageView gridPhoto;
        TextView userNameAgeDistance;

        UserViewHolder(@NonNull View itemView) {
            super(itemView);
            gridPhoto = itemView.findViewById(R.id.grid_photo);
            userNameAgeDistance = itemView.findViewById(R.id.user_name_age_distance);
        }
    }
}