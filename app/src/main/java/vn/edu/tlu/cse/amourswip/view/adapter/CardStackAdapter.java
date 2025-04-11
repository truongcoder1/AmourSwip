package vn.edu.tlu.cse.amourswip.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import vn.edu.tlu.cse.amourswip.R;
import vn.edu.tlu.cse.amourswip.model.data.User;

public class CardStackAdapter extends RecyclerView.Adapter<CardStackAdapter.ViewHolder> {
    private List<User> userList;
    private double currentLat;
    private double currentLon;

    public CardStackAdapter(List<User> userList) {
        this.userList = userList;
    }

    public void setCurrentUserLocation(double lat, double lon) {
        this.currentLat = lat;
        this.currentLon = lon;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = userList.get(position);

        // Tính tuổi từ ngày sinh
        String age = "22"; // Giá trị mặc định
        if (user.getDateOfBirth() != null && !user.getDateOfBirth().isEmpty()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                Date birthDate = sdf.parse(user.getDateOfBirth());
                if (birthDate != null) {
                    long diffInMillies = Math.abs(new Date().getTime() - birthDate.getTime());
                    long diff = diffInMillies / (1000L * 60 * 60 * 24 * 365);
                    age = String.valueOf(diff);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        // Hiển thị tên và tuổi
        holder.userNameAge.setText(user.getName() + ", " + age);

        // Tính khoảng cách
        double distance = calculateDistance(currentLat, currentLon, user.getLatitude(), user.getLongitude());
        holder.userDistance.setText(distance + " KM");

        // Hiển thị trạng thái online/offline (giả định, vì class User không có thuộc tính isOnline)
        holder.userStatus.setText("Active Now"); // Thay bằng logic thực tế nếu có

        // TODO: Sử dụng Glide để tải ảnh từ URL
        // if (user.getPhotos() != null && !user.getPhotos().isEmpty()) {
        //     Glide.with(holder.itemView.getContext()).load(user.getPhotos().get(0)).into(holder.userImage);
        // }
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Bán kính Trái Đất (km)
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c; // Khoảng cách tính bằng km
        return Math.round(distance * 10.0) / 10.0; // Làm tròn đến 1 chữ số thập phân
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView userImage;
        TextView userNameAge;
        TextView userDistance;
        TextView userStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            userImage = itemView.findViewById(R.id.user_image);
            userNameAge = itemView.findViewById(R.id.user_name_age);
            userDistance = itemView.findViewById(R.id.user_distance);
            userStatus = itemView.findViewById(R.id.user_status);
        }
    }
}