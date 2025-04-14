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
import vn.edu.tlu.cse.amourswip.model.data.xUser;

public class chCardStackAdapter extends RecyclerView.Adapter<chCardStackAdapter.ViewHolder> {

    private List<xUser> userList;
    private double currentLatitude;
    private double currentLongitude;

    public chCardStackAdapter(List<xUser> userList) {
        this.userList = userList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        xUser user = userList.get(position);

        // Hiển thị tên và tuổi
        holder.userNameAge.setText(user.getName() + ", " + user.getAge());

        // Hiển thị nơi ở
        String residence = user.getResidence() != null && !user.getResidence().isEmpty() ? user.getResidence() : "Không xác định";
        holder.userResidence.setText(residence);

        // Hiển thị khoảng cách
        String distance = calculateDistance(user.getLatitude(), user.getLongitude());
        holder.userDistance.setText(distance);

        // Tải hình ảnh theo nhu cầu (lazy loading)
        if (user.getPhotos() != null && !user.getPhotos().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(user.getPhotos().get(0))
                    .placeholder(R.drawable.gai1)
                    .error(R.drawable.gai1)
                    .into(holder.userImage);
        } else {
            holder.userImage.setImageResource(R.drawable.gai1);
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

    public void setCurrentUserLocation(double latitude, double longitude) {
        this.currentLatitude = latitude;
        this.currentLongitude = longitude;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView userImage;
        TextView userNameAge;
        TextView userResidence;
        TextView userDistance;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            userImage = itemView.findViewById(R.id.user_image);
            userNameAge = itemView.findViewById(R.id.user_name_age);
            userResidence = itemView.findViewById(R.id.user_residence);
            userDistance = itemView.findViewById(R.id.user_distance);
        }
    }
}