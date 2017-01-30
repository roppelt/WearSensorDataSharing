package com.biankaroppelt.masterthesis.data;

public class TargetInfo {
   private float targetAngle;
   private float maxAngle;
   private float varianceInPercent;

   public TargetInfo(float targetAngle, float maxAngle, float varianceInPercent) {

      this.targetAngle = targetAngle;
      this.maxAngle = maxAngle;
      this.varianceInPercent = varianceInPercent;
   }

   public float getMaxAngle() {
      return maxAngle;
   }

   public float getTargetAngle() {
      return targetAngle;
   }

   public float getVarianceInPercent() {
      return varianceInPercent;
   }
}
