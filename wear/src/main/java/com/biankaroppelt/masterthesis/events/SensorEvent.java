package com.biankaroppelt.masterthesis.events;

import android.hardware.Sensor;

public class SensorEvent {
   public int accuracy;
   public Sensor sensor;
   public long timestamp;
   public float[] values = null;

   public SensorEvent(int accuracy, Sensor sensor, long timestamp, float[] values) {
      this.accuracy = accuracy;
      this.sensor = sensor;
      this.timestamp = timestamp;
      this.values = values;
   }

}