package com.biankaroppelt.masterthesis.data;

public class SensorDataPoint {
   private boolean absolute;
   private int accuracy;
   private Sensor sensor;
   private long timestamp;
   private float[] values;

   public SensorDataPoint(Sensor sensor, long timestamp, int accuracy, boolean absolute,
         float[] values) {
      this.sensor = sensor;
      this.timestamp = timestamp;
      this.accuracy = accuracy;
      this.values = values;
      this.absolute = absolute;
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

   public boolean isAbsolute() {
      return absolute;
   }
}
