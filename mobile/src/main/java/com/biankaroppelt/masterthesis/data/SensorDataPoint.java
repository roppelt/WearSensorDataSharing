package com.biankaroppelt.masterthesis.data;

public class SensorDataPoint {
   private Sensor sensor;
   private long timestamp;
   private float[] values;
   private int accuracy;

   public SensorDataPoint(Sensor sensor, long timestamp, int accuracy, float[] values) {
      this.sensor = sensor;
      this.timestamp = timestamp;
      this.accuracy = accuracy;
      this.values = values;
   }

   public float[] getValues() {
      return values;
   }

   public long getTimestamp() {
      return timestamp;
   }

   public int getAccuracy() {
      return accuracy;
   }

   public Sensor getSensor() {
      return sensor;
   }
}
