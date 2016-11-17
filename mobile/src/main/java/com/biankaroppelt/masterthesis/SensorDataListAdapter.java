package com.biankaroppelt.masterthesis;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.biankaroppelt.masterthesis.data.SensorDataPoint;
import com.biankaroppelt.masterthesis.events.BusProvider;
import com.biankaroppelt.masterthesis.events.DataPointAddedEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

public class SensorDataListAdapter extends RecyclerView.Adapter<SensorDataListViewHolder> {

   private Activity activity;
   private ArrayList<SensorDataPoint> mItems;

   public SensorDataListAdapter(Activity activity, ArrayList<SensorDataPoint> dataPoints) {
      this.activity = activity;
      this.mItems = dataPoints;
   }

   public void addDataPoints(ArrayList<SensorDataPoint> dataPointList) {
//      ArrayList<Long> filterSensorIds = new ArrayList<>();
//      filterSensorIds.add(1L);
//            filterListForSensor(filterSensorIds, dataPointList);
      mItems.addAll(dataPointList);
      notifyDataSetChanged();
      BusProvider.postOnMainThread(new DataPointAddedEvent(getItemCountAccelerometer()));
   }

   private ArrayList<SensorDataPoint> filterListForSensor(ArrayList<Long> sensorIds,
         ArrayList<SensorDataPoint> dataList) {
      Iterator<SensorDataPoint> it = dataList.iterator();
      while (it.hasNext()) {
         if (!sensorIds.contains(it.next()
               .getSensor()
               .getId())) {
            it.remove();
         }
      }
      return dataList;
   }

   public void deleteData() {
      mItems = new ArrayList<>();
      notifyDataSetChanged();
   }

   public int getItemCountAccelerometer() {
      int count = 0;
      for (SensorDataPoint dataPoint : mItems) {
         if (dataPoint.getSensor()
               .getId() == 1) {
            count++;
         }
      }
      return count;
   }

   public ArrayList<SensorDataPoint> getItems() {
      return mItems;
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
      viewHolder.sensorInfo2.setText(Arrays.toString(mItems.get(i)
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

   public SensorDataListViewHolder(View itemView) {
      super(itemView);

      sensorInfo1 = (TextView) itemView.findViewById(R.id.sensor_info_1);
      sensorInfo2 = (TextView) itemView.findViewById(R.id.sensor_info_2);
   }
}

