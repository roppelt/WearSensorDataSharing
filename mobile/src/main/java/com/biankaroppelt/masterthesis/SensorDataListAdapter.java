package com.biankaroppelt.masterthesis;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.biankaroppelt.masterthesis.data.SensorDataPoint;

import java.util.ArrayList;
import java.util.Arrays;

public class SensorDataListAdapter extends RecyclerView.Adapter<SensorDataListViewHolder> {

   private Activity activity;
   private ArrayList<SensorDataPoint> mItems;

   public SensorDataListAdapter(Activity activity, ArrayList<SensorDataPoint> dataPoints) {
      this.activity = activity;
      this.mItems = dataPoints;
   }

   public void addDataPoint(SensorDataPoint dataPoint) {
      mItems.add(0, dataPoint);
      notifyItemInserted(0);
   }

   @Override
   public SensorDataListViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
      View view = LayoutInflater.from(viewGroup.getContext())
            .inflate(R.layout.item_sensor_data, viewGroup, false);
      return new SensorDataListViewHolder(view);
   }

   @Override
   public void onBindViewHolder(SensorDataListViewHolder viewHolder, int i) {
      viewHolder.sensorInfo1.setText(mItems.get(i)
            .getSensor()
            .getName());
      viewHolder.sensorInfo2.setText(String.valueOf(mItems.get(i)
            .getAccuracy()));
      viewHolder.sensorInfo3.setText(Arrays.toString(mItems.get(i)
            .getValues()));
   }

   @Override
   public int getItemCount() {
      return mItems.size();
   }
}

class SensorDataListViewHolder extends RecyclerView.ViewHolder {

   TextView sensorInfo1;
   TextView sensorInfo2;
   TextView sensorInfo3;

   public SensorDataListViewHolder(View itemView) {
      super(itemView);

      sensorInfo1 = (TextView) itemView.findViewById(R.id.sensor_info_1);
      sensorInfo2 = (TextView) itemView.findViewById(R.id.sensor_info_2);
      sensorInfo3 = (TextView) itemView.findViewById(R.id.sensor_info_3);
   }
}

