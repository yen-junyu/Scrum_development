package com.example.sean.bei_na_song_liechat.Adapter;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.example.sean.bei_na_song_liechat.MainActivity;
import com.example.sean.bei_na_song_liechat.MessageActivity;

import java.util.ArrayList;

public class StickerAdapter extends BaseAdapter {
    private Activity mContext;

    // Keep all Images in array
    public ArrayList<Integer> mThumbIds;

    // Constructor
    public StickerAdapter(MessageActivity messageActivity, ArrayList<Integer> items) {
        this.mContext = messageActivity;
        this.mThumbIds = items;
    }

    @Override
    public int getCount() {
        return mThumbIds.size();
    }

    @Override
    public Object getItem(int position) {
        return mThumbIds.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView = new ImageView(mContext);
        imageView.setImageResource(mThumbIds.get(position));
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setLayoutParams(new GridView.LayoutParams(400, 400));
        return imageView;
    }
}
