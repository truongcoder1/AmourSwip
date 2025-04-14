package vn.edu.tlu.cse.amourswip.view.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import vn.edu.tlu.cse.amourswip.R;
import vn.edu.tlu.cse.amourswip.model.data.xUser;

public class chUserGridAdapter extends RecyclerView.Adapter<chUserGridAdapter.UserViewHolder> {

    private static final String TAG = "chUserGridAdapter";
    private List<xUser> userList;
    private final Consumer<xUser> onUserClicked;
    private double currentLatitude;
    private double currentLongitude;
    private boolean showActionButtons;
    private Consumer<xUser> onLikeClicked;
    private Consumer<xUser> onDislikeClicked;

    public chUserGridAdapter(List<xUser> userList, Consumer<xUser> onUserClicked, double currentLatitude, double currentLongitude) {
        this.userList = userList != null ? userList : new ArrayList<>();
        this.onUserClicked = onUserClicked;
        this.currentLatitude = currentLatitude;
        this.currentLongitude = currentLongitude;
        this.showActionButtons = false;
    }

    public void setShowActionButtons(boolean show, Consumer<xUser> onLikeClicked, Consumer<xUser> onDislikeClicked) {
        Log.d(TAG, "setShowActionButtons: show=" + show);
        this.showActionButtons = show;
        this.onLikeClicked = onLikeClicked;
        this.onDislikeClicked = onDislikeClicked;
        // Không gọi notifyDataSetChanged() ở đây, vì chLikeFragment đã sử dụng DiffUtil
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_grid_photo, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        xUser user = userList.get(position);

        // Hiển thị tên và tuổi
        holder.userNameAge.setText(user.getName() + ", " + user.getAge());

        // Hiển thị nơi ở
        String residence = user.getResidence() != null && !user.getResidence().isEmpty() ? user.getResidence() : "Không xác định";
        holder.userResidence.setText(residence);

        // Hiển thị khoảng cách
        String distance = calculateDistance(user.getLatitude(), user.getLongitude());
        holder.userDistance.setText(distance);

        if (user.getPhotos() != null && !user.getPhotos().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(user.getPhotos().get(0))
                    .placeholder(R.drawable.gai1)
                    .error(R.drawable.gai1)
                    .into(holder.userImage);
        } else {
            holder.userImage.setImageResource(R.drawable.gai1);
        }

        holder.itemView.setOnClickListener(v -> onUserClicked.accept(user));

        if (showActionButtons) {
            Log.d(TAG, "onBindViewHolder: Showing action buttons for user " + user.getName());
            holder.actionButtons.setVisibility(View.VISIBLE);
            holder.likeButton.setOnClickListener(v -> {
                Log.d(TAG, "onBindViewHolder: Like button clicked for user " + user.getName());
                if (onLikeClicked != null) {
                    onLikeClicked.accept(user);
                }
            });
            holder.dislikeButton.setOnClickListener(v -> {
                Log.d(TAG, "onBindViewHolder: Dislike button clicked for user " + user.getName());
                if (onDislikeClicked != null) {
                    onDislikeClicked.accept(user);
                }
            });
        } else {
            Log.d(TAG, "onBindViewHolder: Hiding action buttons for user " + user.getName());
            holder.actionButtons.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    private String calculateDistance(double latitude, double longitude) {
        if (currentLatitude == 0 || currentLongitude == 0) {
            return "N/A";
        }

        android.location.Location currentLocation = new android.location.Location("");
        currentLocation.setLatitude(currentLatitude);
        currentLocation.setLongitude(currentLongitude);

        android.location.Location userLocation = new android.location.Location("");
        userLocation.setLatitude(latitude);
        userLocation.setLongitude(longitude);

        float distanceInMeters = currentLocation.distanceTo(userLocation);
        float distanceInKm = distanceInMeters / 1000;
        return String.format("%.1f KM", distanceInKm);
    }

    public void updateList(List<xUser> newList) {
        this.userList = newList != null ? newList : new ArrayList<>();
        // Không gọi notifyDataSetChanged() ở đây, vì chLikeFragment đã sử dụng DiffUtil
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        ImageView userImage;
        TextView userNameAge;
        TextView userResidence;
        TextView userDistance;
        LinearLayout actionButtons;
        ImageButton likeButton;
        ImageButton dislikeButton;

        UserViewHolder(@NonNull View itemView) {
            super(itemView);
            userImage = itemView.findViewById(R.id.grid_photo);
            userNameAge = itemView.findViewById(R.id.user_name_age);
            userResidence = itemView.findViewById(R.id.user_residence);
            userDistance = itemView.findViewById(R.id.user_distance);
            actionButtons = itemView.findViewById(R.id.action_buttons);
            likeButton = itemView.findViewById(R.id.like_button);
            dislikeButton = itemView.findViewById(R.id.dislike_button);
        }
    }
}