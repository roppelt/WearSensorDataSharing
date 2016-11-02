package com.biankaroppelt.masterthesis.data;

import java.util.LinkedList;

public class Sensor {
   private static final String TAG = Sensor.class.getName();
   private static final int MAX_DATA_POINTS = 10000;

   private long id;
   private String name;

   private LinkedList<SensorDataPoint> dataPoints = new LinkedList<>();

   public Sensor(int id, String name) {
      this.id = id;
      this.name = name;
   }

   public String getName() {
      return name;
   }

   public synchronized LinkedList<SensorDataPoint> getDataPoints() {
      return (LinkedList<SensorDataPoint>) dataPoints.clone();
   }

   public synchronized void addDataPoint(SensorDataPoint dataPoint) {
      dataPoints.addLast(dataPoint);

      if (dataPoints.size() > MAX_DATA_POINTS) {
         dataPoints.removeFirst();
      }
   }

   public long getId() {
      return id;
   }
}
