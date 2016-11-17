package com.biankaroppelt.masterthesis.events;

import com.biankaroppelt.masterthesis.data.Sensor;
import com.biankaroppelt.masterthesis.data.SensorDataPoint;

public class DataPointAddedEvent {
   private int count;

   public DataPointAddedEvent(int newCount) {
      this.count = newCount;
   }

   public int getCount() {
      return count;
   }
}
