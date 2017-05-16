package com.biankaroppelt.masterthesis.data;

import android.hardware.Sensor;

public class SensorDataPoint {
   private int accuracy;
   private Sensor sensor;
   private long timestamp;
   private float[] values;

   public SensorDataPoint(Sensor sensor, long timestamp, int accuracy, float[] values) {
      this.sensor = sensor;
      this.timestamp = timestamp;
      this.accuracy = accuracy;
      this.values = values;
   }

   public int getAccuracy() {
      return accuracy;
   }

   public Sensor getSensor() {
      return sensor;
   }

   public long getTimestamp() {
      return timestamp;
   }

   public float[] getValues() {
      return values;
   }
}
