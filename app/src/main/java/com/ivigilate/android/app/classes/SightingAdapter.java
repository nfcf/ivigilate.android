package com.ivigilate.android.app.classes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ivigilate.android.app.R;
import com.ivigilate.android.library.utils.StringUtils;

import java.util.LinkedHashMap;

public class SightingAdapter extends BaseAdapter {

    private final LinkedHashMap<String, SimpleSighting> mValues;

    public SightingAdapter(LinkedHashMap<String, SimpleSighting> values) {
        mValues = values;
    }

    @Override
    public int getCount() {
        return mValues.size();
    }

    @Override
    public SimpleSighting getItem(int position) {
        return  mValues.get(mValues.keySet().toArray()[position]);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) parent.getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.sighting, parent, false);

        ImageView ivType = (ImageView) rowView.findViewById(R.id.ivType);
        //set the icon like so: ivType.setImageResource(R.drawable.no);

        TextView tvMacValue = (TextView) rowView.findViewById(R.id.tvMacValue);
        String mac = getItem(position).mac;
        tvMacValue.setText(mac != null ? mac : "");

        TextView tvRssiValue = (TextView) rowView.findViewById(R.id.tvRssiValue);
        tvRssiValue.setText(Integer.toString(getItem(position).rssi));

        TextView tvUidValue = (TextView) rowView.findViewById(R.id.tvUuidValue);
        String uid = getItem(position).uuid;
        tvUidValue.setText(!StringUtils.isNullOrBlank(uid) ? uid : "N/A");

        return rowView;
    }

}
