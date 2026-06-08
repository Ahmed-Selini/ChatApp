package com.example.chatproject;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class DMAdapter extends RecyclerView.Adapter<DMAdapter.DMHolder> {

    ArrayList<DM> list;

    public DMAdapter(ArrayList<DM> list) {
        this.list = list;
    }

    @Override
    public DMHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_dm, parent, false);
        return new DMHolder(v);
    }

    @Override
    public void onBindViewHolder(DMHolder h, int pos) {
        DM d = list.get(pos);
        h.profileImage.setImageResource(d.image);
        h.nameText.setText(d.name);
        h.messageText.setText(d.message);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class DMHolder extends RecyclerView.ViewHolder {

        ImageView profileImage;
        TextView nameText, messageText;

        DMHolder(View item) {
            super(item);
            profileImage = item.findViewById(R.id.profileImage);
            nameText = item.findViewById(R.id.nameText);
            messageText = item.findViewById(R.id.messageText);
        }
    }
}
