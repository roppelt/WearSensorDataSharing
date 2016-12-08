package com.biankaroppelt.masterthesis.events;

public class DataPointAddedEvent {
   private int count;

   public DataPointAddedEvent(int newCount) {
      this.count = newCount;
   }

   public int getCount() {
      return count;
   }
}
