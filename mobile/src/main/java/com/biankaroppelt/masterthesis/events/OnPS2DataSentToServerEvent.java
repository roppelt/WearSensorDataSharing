package com.biankaroppelt.masterthesis.events;

public class OnPS2DataSentToServerEvent {
   private int rotationDimension;
   private boolean success;
   private int target;
   private boolean taskSuccess;

   public OnPS2DataSentToServerEvent(boolean success, int target, int rotationDimension,
         boolean taskSuccess) {
      this.success = success;
      this.target = target;
      this.rotationDimension = rotationDimension;
      this.taskSuccess = taskSuccess;
   }

   public int getRotationDimension() {
      return rotationDimension;
   }

   public int getTarget() {
      return target;
   }

   public boolean getTaskSuccess() {
      return taskSuccess;
   }

   public boolean isSuccess() {
      return success;
   }
}
