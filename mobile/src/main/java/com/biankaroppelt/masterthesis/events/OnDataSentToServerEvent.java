package com.biankaroppelt.masterthesis.events;

public class OnDataSentToServerEvent {
   private String resultInfo;

   public OnDataSentToServerEvent(String resultInfo) {
      this.resultInfo = resultInfo;
   }

   public String getResultInfo() {
      return resultInfo;
   }
}
