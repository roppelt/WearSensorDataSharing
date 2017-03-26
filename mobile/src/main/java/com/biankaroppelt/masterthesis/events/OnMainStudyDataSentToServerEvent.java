package com.biankaroppelt.masterthesis.events;

public class OnMainStudyDataSentToServerEvent {
   private boolean success;

   public OnMainStudyDataSentToServerEvent(boolean success) {
      this.success = success;
   }

   public boolean isSuccess() {
      return success;
   }
}
