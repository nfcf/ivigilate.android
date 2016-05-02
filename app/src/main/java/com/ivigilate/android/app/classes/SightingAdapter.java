package com.ivigilate.android.app.classes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
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

        TextView tvMacValue = (TextView) rowView.findViewById(R.id.tvMacValue);
        String mac = getItem(position).getMac();
        tvMacValue.setText(mac != null ? mac : "");

        TextView tvRssiValue = (TextView) rowView.findViewById(R.id.tvRssiValue);
        tvRssiValue.setText(Integer.toString(getItem(position).getRssi()));

        TextView tvUuidValue = (TextView) rowView.findViewById(R.id.tvUuidValue);
        String uuid = getItem(position).getUUID();
        tvUuidValue.setText(!StringUtils.isNullOrBlank(uuid) ? uuid : "N/A");

        TextView tvDataValue = (TextView) rowView.findViewById(R.id.tvDataValue);
        String data = getItem(position).getData();
        tvDataValue.setText(!StringUtils.isNullOrBlank(data) ? data : "N/A");

        TextView tvManufacturerValue = (TextView) rowView.findViewById(R.id.tvManufacturerValue);
        String manufacturer = getItem(position).getManufacturer();
        tvManufacturerValue.setText(!StringUtils.isNullOrBlank(manufacturer) ? manufacturer : "N/A");

        TextView tvTypeValue = (TextView) rowView.findViewById(R.id.tvTypeValue);
        String type = getItem(position).getBleType();
        tvTypeValue.setText(!StringUtils.isNullOrBlank(type) ? type : "N/A");


        return rowView;
    }

}
