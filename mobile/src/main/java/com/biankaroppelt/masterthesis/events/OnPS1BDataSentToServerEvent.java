package com.biankaroppelt.masterthesis.events;

public class OnPS1BDataSentToServerEvent {
   private boolean success;
   private int tapType;

   public OnPS1BDataSentToServerEvent(boolean success, int tapType) {
      this.success = success;
      this.tapType = tapType;
   }

   public int getTapType() {
      return tapType;
   }

   public boolean isSuccess() {
      return success;
   }
}
