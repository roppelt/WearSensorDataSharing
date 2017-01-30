package com.biankaroppelt.masterthesis.events;

public class OnDataSentToServerEvent {
   private boolean success;

   public OnDataSentToServerEvent(boolean success) {
      this.success = success;
   }

   public boolean isSuccess() {
      return success;
   }
}
