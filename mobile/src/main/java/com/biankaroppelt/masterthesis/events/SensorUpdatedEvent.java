package com.biankaroppelt.masterthesis.events;

import com.biankaroppelt.masterthesis.data.SensorDataPoint;

import java.util.ArrayList;

public class SensorUpdatedEvent {

   private ArrayList<SensorDataPoint> sensorDataPointList;

   public SensorUpdatedEvent(ArrayList<SensorDataPoint> sensorDataPointList) {
      this.sensorDataPointList = sensorDataPointList;
   }

   public ArrayList<SensorDataPoint> getDataPointList() {
      return sensorDataPointList;
   }
}
