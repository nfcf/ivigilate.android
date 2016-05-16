package com.ivigilate.android.app.classes;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ivigilate.android.app.R;
import com.ivigilate.android.library.classes.DeviceSighting;
import com.ivigilate.android.library.utils.StringUtils;

import java.util.LinkedHashMap;

public class SightingAdapter extends BaseAdapter {

    private final LinkedHashMap<String, DeviceSighting> mValues;

    public SightingAdapter(LinkedHashMap<String, DeviceSighting> values) {
        mValues = values;
    }

    @Override
    public int getCount() {
        return mValues.size();
    }

    @Override
    public DeviceSighting getItem(int position) {
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
        DeviceSighting currDevice = getItem(position);

        if(currDevice.isProvisioned()) {
            RelativeLayout rlSighting = (RelativeLayout) rowView.findViewById(R.id.layout);
            rlSighting.setBackgroundColor(Color.parseColor("#D3FFCE"));

            ImageView ivTypeIcon = (ImageView) rowView.findViewById(R.id.ivTypeIcon);
            ivTypeIcon.setImageResource(currDevice.getTypeIconId());
        }

        TextView tvMacValue = (TextView) rowView.findViewById(R.id.tvMacValue);
        String mac = currDevice.getMac();
        tvMacValue.setText(!StringUtils.isNullOrBlank(mac) ? mac : "N/A");

        TextView tvNameValue = (TextView) rowView.findViewById(R.id.tvNameValue);
        String name = currDevice.getName();
        tvNameValue.setText(name != null ? name : "(Unknown)");

        TextView tvRssiValue = (TextView) rowView.findViewById(R.id.tvRssiValue);
        tvRssiValue.setText(Integer.toString(currDevice.getRssi()));

        TextView tvUuidValue = (TextView) rowView.findViewById(R.id.tvUuidValue);
        String uuid = currDevice.getUUID();
        tvUuidValue.setText(!StringUtils.isNullOrBlank(uuid) ? uuid : "N/A");

        TextView tvDataValue = (TextView) rowView.findViewById(R.id.tvDataValue);
        String data = currDevice.getData();
        tvDataValue.setText(!StringUtils.isNullOrBlank(data) ? data : "N/A");

        TextView tvManufacturerValue = (TextView) rowView.findViewById(R.id.tvManufacturerValue);
        String manufacturer = currDevice.getManufacturer();
        tvManufacturerValue.setText(!StringUtils.isNullOrBlank(manufacturer) ? manufacturer : "N/A");

        TextView tvTypeValue = (TextView) rowView.findViewById(R.id.tvTypeValue);
        String type = currDevice.getBleType();
        tvTypeValue.setText(!StringUtils.isNullOrBlank(type) ? type : "N/A");


        return rowView;
    }

}
