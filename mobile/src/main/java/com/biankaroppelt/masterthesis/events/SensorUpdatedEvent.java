package com.biankaroppelt.masterthesis.events;

import com.biankaroppelt.masterthesis.data.Sensor;
import com.biankaroppelt.masterthesis.data.SensorDataPoint;

public class SensorUpdatedEvent {
   private Sensor sensor;
   private SensorDataPoint sensorDataPoint;

   public SensorUpdatedEvent(Sensor sensor, SensorDataPoint sensorDataPoint) {
      this.sensor = sensor;
      this.sensorDataPoint = sensorDataPoint;
   }

   public Sensor getSensor() {
      return sensor;
   }

   public SensorDataPoint getDataPoint() {
      return sensorDataPoint;
   }
}
