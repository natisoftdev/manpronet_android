package com.natisoftnavigazione;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MyAdapter extends BaseAdapter {

    private String[] data;
    private Integer[] imgsResource;
    private Context context;
    private int mSelectedItem;

    public MyAdapter(Context context, String[] data1, Integer[] imgsResource) {
        super();
        this.data = data1;
        this.imgsResource = imgsResource;
        this.context = context;
    }

    @Override
    public int getCount() {
        return data.length;
    }

    @Override
    public Object getItem(int position) {
        return data[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setSelectedItem(int selectedItem) {
        mSelectedItem = selectedItem;
    }

    @SuppressLint("ViewHolder")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = LayoutInflater.from(context).
                inflate(R.layout.two_line_icon, parent, false);

        TextView text1 = rowView.findViewById(R.id.text1);
        ImageView icon = rowView.findViewById(R.id.icon);

        text1.setText(data[position]);
        icon.setImageResource(imgsResource[position]);

        LinearLayout ly = rowView.findViewById(R.id.linearLayout1);

        if (position == mSelectedItem) {
            ly.setBackgroundColor(Color.GRAY);
            text1.setTextColor(Color.WHITE);

        } else {
            ly.setBackgroundColor(Color.TRANSPARENT);
            text1.setTextColor(Color.DKGRAY);
        }
        return rowView;
    }

}