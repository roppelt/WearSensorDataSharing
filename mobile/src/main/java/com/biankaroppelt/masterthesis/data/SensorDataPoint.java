package com.biankaroppelt.masterthesis.data;

public class SensorDataPoint {
   private Sensor sensor;
   private long timestamp;
   private float[] values;
   private int accuracy;
   private boolean absolute;

   public SensorDataPoint(Sensor sensor, long timestamp, int accuracy, boolean absolute,
         float[] values) {
      this.sensor = sensor;
      this.timestamp = timestamp;
      this.accuracy = accuracy;
      this.values = values;
      this.absolute = absolute;
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

   public boolean isAbsolute() {
      return absolute;
   }
}
