package com.example.eaglereader;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class MediaAdapter extends RecyclerView.Adapter<MediaAdapter.ViewHolder> {
    private Context context;
    private List<MainActivity.MediaItem> items;

    public MediaAdapter(Context context, List<MainActivity.MediaItem> items) {
        this.context = context;
        this.items = items;
    }

    public void updateData(List<MainActivity.MediaItem> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_media, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        MainActivity.MediaItem item = items.get(position);
        holder.nameText.setText(item.name);
        holder.infoText.setText(item.width + "x" + item.height + " | " + item.ext.toUpperCase());
        holder.starText.setText(getStarString(item.star));
    }

    private String getStarString(int star) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            sb.append(i < star ? "\u2605" : "\u2606");
        }
        return sb.toString();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView thumbnail;
        TextView nameText;
        TextView infoText;
        TextView starText;

        ViewHolder(View itemView) {
            super(itemView);
            thumbnail = itemView.findViewById(R.id.thumbnail);
            nameText = itemView.findViewById(R.id.nameText);
            infoText = itemView.findViewById(R.id.infoText);
            starText = itemView.findViewById(R.id.starText);
        }
    }
}