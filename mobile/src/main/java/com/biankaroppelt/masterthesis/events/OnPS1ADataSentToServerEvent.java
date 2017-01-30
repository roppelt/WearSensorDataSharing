package com.biankaroppelt.masterthesis.events;

public class OnPS1ADataSentToServerEvent {
   private boolean success;
   private int finger;
   private int rotationDimension;

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
