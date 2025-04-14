package vn.edu.tlu.cse.amourswip.view.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import java.util.List;

import vn.edu.tlu.cse.amourswip.R;

public class trPhotoAdapter extends BaseAdapter {

    private Context context;
    private List<String> photoUrls;

    public trPhotoAdapter(Context context, List<String> photoUrls) {
        this.context = context;
        this.photoUrls = photoUrls;
    }

    @Override
    public int getCount() {
        return photoUrls.size();
    }

    @Override
    public Object getItem(int position) {
        return photoUrls.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_not_infor_photo, parent, false);
        }

        ImageView imageView = convertView.findViewById(R.id.photo_image);
        String photoUrl = photoUrls.get(position);

        // Sử dụng Glide để tải ảnh từ URL
        Glide.with(context)
                .load(photoUrl)
                .placeholder(R.drawable.gai1)
                .error(R.drawable.gai1)
                .into(imageView);

        return convertView;
    }
}