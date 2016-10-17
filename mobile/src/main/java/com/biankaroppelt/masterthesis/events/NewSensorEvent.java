package com.biankaroppelt.masterthesis.events;

import com.biankaroppelt.masterthesis.data.Sensor;

public class NewSensorEvent {
   private Sensor sensor;

   public NewSensorEvent(Sensor sensor) {
      this.sensor = sensor;
   }

   public Sensor getSensor() {
      return sensor;
   }
}
