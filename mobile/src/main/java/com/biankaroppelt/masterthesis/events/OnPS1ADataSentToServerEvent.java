package com.biankaroppelt.masterthesis.events;

public class OnPS1ADataSentToServerEvent {
   private int finger;
   private int rotationDimension;
   private boolean success;

   public OnPS1ADataSentToServerEvent(boolean success, int finger, int rotationDimension) {
      this.success = success;
      this.finger = finger;
      this.rotationDimension = rotationDimension;
   }

   public int getFinger() {
      return finger;
   }

   public int getRotationDimension() {
      return rotationDimension;
   }

   public boolean isSuccess() {
      return success;
   }
}
