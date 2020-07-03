package com.example.cosc_project;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

public class CustomAdapter extends ArrayAdapter<DataModel> implements View.OnClickListener{

    private ArrayList<DataModel> dataSet;
    Context mContext;
    private static class ViewHolder {
            TextView exam_name;
            TextView details;
    }
    public CustomAdapter(ArrayList < DataModel > data, Context context) {
        super(context, R.layout.my_listview_detail, data);
        this.dataSet = data;
        this.mContext = context;
    }
        public void onClick (View v){

        int position = (Integer) v.getTag();
        Object object = getItem(position);
        DataModel dataModel = (DataModel) object;
        /*
        switch (v.getId()) {
            case R.id.details:
                Snackbar.make(v, "Release date " + dataModel.getDetails(), Snackbar.LENGTH_LONG)
                        .setAction("No action", null).show();
                break;
        }

         */
    }

        private int lastPosition = -1;

        @Override
        public View getView ( int position, View convertView, ViewGroup parent){

            DataModel dataModel = getItem(position);
            ViewHolder viewHolder;

            final View result;

            if (convertView == null) {

                viewHolder = new ViewHolder();
                LayoutInflater inflater = LayoutInflater.from(getContext());
                convertView = inflater.inflate(R.layout.my_listview_detail, parent, false);
                viewHolder.exam_name = (TextView) convertView.findViewById(R.id.exam_name);
                viewHolder.details = (TextView) convertView.findViewById(R.id.details);

                result = convertView;
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
                result = convertView;
            }

            Animation animation = AnimationUtils.loadAnimation(mContext, (position > lastPosition) ? R.layout.up_from_bottom : R.layout.down_from_top);
            result.startAnimation(animation);
            lastPosition = position;

            viewHolder.exam_name.setText(dataModel.getExamName());
            viewHolder.details.setText(dataModel.getDetails());

            // Return the completed view to render on screen
            return convertView;
    }
}
