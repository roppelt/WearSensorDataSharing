package com.biankaroppelt.masterthesis;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import java.util.Arrays;

public class SensorServiceOrientation extends Service implements SensorEventListener {
   private static final String TAG = SensorServiceOrientation.class.getName();

   private final static int SENS_ACCELEROMETER = Sensor.TYPE_ACCELEROMETER;
   private final static int SENS_GYROSCOPE = Sensor.TYPE_GYROSCOPE;
   private final static int SENS_GAME_ROTATION_VECTOR = Sensor.TYPE_GAME_ROTATION_VECTOR;

   SensorManager mSensorManager;

   private DeviceClient client;
   private final float[] mAngleChanges = new float[9];
   //   private float[] mStartValuesGameRotation;
   private final float[] mRotationMatrixGame = new float[9];
   //   private final float[] mRotationMatrixGameFromVector = new float[9];
   private float[] mRotationMatrixGameStart;
   private float lastValue1 = 0.0f;
   private float lastValue2 = 0.0f;
   private float lastValue3 = 0.0f;
   //   private float[] mRotationMatrixGameLast;
   //   private float[] mRotationVectorStart;
   //   private float[] mAngleChangesRelativeToLast = new float[3];
   //   private float[] mOrientationValues = new float[3];
   //   private float[] mOrientationValuesStart = new float[3];
   //   private float[] mStartValues;
   //   private float[] mLastValues;
   //   private boolean changeValues = false;

   private int count = 0;
   private boolean getAbsoluteValues = false;

   public SensorServiceOrientation() {
   }

   @Override
   public void onCreate() {
      super.onCreate();

      client = DeviceClient.getInstance(this);

      startMeasurementOrientation();
   }

   @Override
   public void onStart(Intent intent, int startId) {
      super.onStart(intent, startId);
      if (intent != null) {
         Bundle extras = intent.getExtras();

         if (extras == null) {
            Log.d("Service", "null");
         } else {
            Log.d("Service", "not null");
            getAbsoluteValues = extras.getBoolean("absolute");
         }
      }
   }

   @Override
   public void onDestroy() {
      super.onDestroy();

      stopMeasurementOrientation();
   }

   @Override
   public IBinder onBind(Intent intent) {
      return null;
   }

   protected void startMeasurementOrientation() {
      count = 0;
      mSensorManager = ((SensorManager) getSystemService(SENSOR_SERVICE));

      Sensor gameRotationVectorSensor = mSensorManager.getDefaultSensor(SENS_GAME_ROTATION_VECTOR);

      mSensorManager.registerListener(this, gameRotationVectorSensor,
            SensorManager.SENSOR_DELAY_GAME, SensorManager.SENSOR_DELAY_UI);
   }

   private void stopMeasurementOrientation() {
      if (mSensorManager != null) {
         mSensorManager.unregisterListener(this);
      }
   }

   public static float dot(float[] v1, float[] v2) {
      float res = 0;
      for (int i = 0; i < v1.length; i++) {
         res += v1[i] * v2[i];
      }
      return res;
   }

   @Override
   public void onSensorChanged(android.hardware.SensorEvent event) {

      if (event.sensor.getType() == Sensor.TYPE_GAME_ROTATION_VECTOR) {
         count++;
         System.out.println("onSensorChanged: " + count + " - " + event.sensor.getName() + " - " +
               Arrays.toString(event.values));

         //         if (mStartValues == null) {
         //            mStartValues = event.values.clone();
         //         }
         //         double yawStart = Math.toDegrees(Math.asin(mStartValues[2]) * 2);
         //         double yaw = ((Math.toDegrees(Math.asin(event.values[2]) * 2) - yawStart) +
         // 360.0) % 360.0;
         //         System.out.println("Yaw: " + yaw);
         //         double pitchStart = Math.toDegrees(Math.asin(mStartValues[1]) * 2);
         //         double pitch =
         //               ((Math.toDegrees(Math.asin(event.values[1]) * 2) - pitchStart) + 360.0)
         // % 360.0;
         //         System.out.println("Pitch: " + pitch);
         //         double rollStart = Math.toDegrees(Math.asin(mStartValues[0]) * 2);
         //         double roll =
         //               ((Math.toDegrees(Math.asin(event.values[0]) * 2) - rollStart) + 360.0)
         // % 360.0;
         //         System.out.println("Roll: " + roll);
         //         System.out.println("Winkel: " + Math.toDegrees(Math.acos(event.values[3]) * 2));
         //         System.out.println("Winkel1: " + Math.toDegrees(Math.asin(event.values[0]) *
         // 2));
         //         System.out.println("Winkel2: " + Math.toDegrees(Math.asin(event.values[1]) *
         // 2));
         //         System.out.println("Winkel3: " + Math.toDegrees(Math.asin(event.values[2]) *
         // 2));

         ////          Orientation
         SensorManager.getRotationMatrixFromVector(mRotationMatrixGame, event.values.clone());
         //         SensorManager.getOrientation(mRotationMatrixGame, mOrientationValues);
         //         System.out.println("Orientation: " + Arrays.toString(mOrientationValues));
         //         String orientationInAngles = "";
         //         for (int i = 0; i < mOrientationValues.length; i++) {
         //            orientationInAngles += "   " + Math.toDegrees(mOrientationValues[i]);
         //         }
         //         System.out.println("Orientation in angles:" + orientationInAngles);

         if (mRotationMatrixGameStart == null) {
            mRotationMatrixGameStart = mRotationMatrixGame.clone();
            System.out.println(
                  "T Start:  " + mRotationMatrixGameStart[0] + "   " + mRotationMatrixGameStart[1] +
                        "   " + mRotationMatrixGameStart[2]);
         }

         SensorManager.getAngleChange(mAngleChanges, mRotationMatrixGame, mRotationMatrixGameStart);

         float[] dataToSend = new float[3];
         float[] angleChangesCopy = mAngleChanges.clone();

         //         System.out.println("Angle Roll: " +
         //               Math.toDegrees(Math.atan2(mRotationMatrixGame[7],
         // mRotationMatrixGame[8])));
         //         System.out.println("Angle Pitch: " + Math.toDegrees(Math.atan2
         // (-mRotationMatrixGame[6],
         //               Math.sqrt(mRotationMatrixGame[7] * mRotationMatrixGame[7] +
         //                     mRotationMatrixGame[8] * mRotationMatrixGame[8]))));
         //         System.out.println("Angle Yaw: " +
         //               Math.toDegrees(Math.atan2(mRotationMatrixGame[3],
         // mRotationMatrixGame[0])));

         //            dataToSend[1] = (float) Math.toDegrees(angleChangesCopy[1]);

         // Rotate/Yaw
         lastValue1 = getAngleChange360(0, (float) Math.toDegrees(angleChangesCopy[0]));
         dataToSend[0] = lastValue1;
         //         dataToSend[0] = (float) (
         //               Math.toDegrees(Math.atan2(mRotationMatrixGame[3],
         // mRotationMatrixGame[0])) -
         //                     Math.toDegrees(Math.atan2(mRotationMatrixGameStart[3],
         // mRotationMatrixGameStart[0])));

         // Pitch
         lastValue3 = getAngleChange360(2, (float) Math.toDegrees(angleChangesCopy[2]));
         dataToSend[2] = lastValue3;
         //         dataToSend[2] = (float) (Math.toDegrees(Math.atan2(-mRotationMatrixGame[6],
         // Math.sqrt(
         //               mRotationMatrixGame[7] * mRotationMatrixGame[7] +
         //                     mRotationMatrixGame[8] * mRotationMatrixGame[8]))) - Math.toDegrees(
         //               Math.atan2(-mRotationMatrixGameStart[6], Math.sqrt(
         //                     mRotationMatrixGameStart[7] * mRotationMatrixGameStart[7] +
         //                           mRotationMatrixGameStart[8] * mRotationMatrixGameStart[8]))));

         lastValue2 = getAngleChange360(1, (float) (Math.toDegrees(
               Math.atan2(mRotationMatrixGameStart[7], mRotationMatrixGameStart[8])) -
               Math.toDegrees(Math.atan2(mRotationMatrixGame[7], mRotationMatrixGame[8]))));
         dataToSend[1] = lastValue2;
         //
         //         System.out.println("Angle 1 First: " + Math.toDegrees(
         //               Math.atan2(mRotationMatrixGameStart[7], mRotationMatrixGameStart[8])));
         //         System.out.println("Angle 1 Second: " +
         //               Math.toDegrees(Math.atan2(mRotationMatrixGame[7],
         // mRotationMatrixGame[8])));
         //         dataToSend[1] = (float) (Math.toDegrees(
         //               Math.atan2(mRotationMatrixGameStart[7], mRotationMatrixGameStart[8])) -
         //               (Math.toDegrees(Math.atan2(mRotationMatrixGame[7],
         // mRotationMatrixGame[8]))));

         System.out.println(
               "Sending:   " + dataToSend[0] + "    " + dataToSend[1] + "    " + dataToSend[2]);
         System.out.println("Sending Old:   " + Math.toDegrees(angleChangesCopy[0]) + "    " +
               (Math.toDegrees(
                     Math.atan2(mRotationMatrixGameStart[7], mRotationMatrixGameStart[8])) -
                     (Math.toDegrees(Math.atan2(mRotationMatrixGame[7], mRotationMatrixGame[8])))) +
               "    " + Math.toDegrees(angleChangesCopy[2]));
         System.out.println(
               "Diff output: " + (Math.toDegrees(angleChangesCopy[1]) - dataToSend[1]) + "     " +
                     Math.toDegrees(angleChangesCopy[1]) + "  -  " + dataToSend[1]);

         long timeInMillis = (System.currentTimeMillis() +
               Math.round((event.timestamp - SystemClock.elapsedRealtimeNanos()) / 1000000L));
         //               (new Date()).getTime() + (event.timestamp - System.nanoTime()) / 1000000L;

         client.sendSensorDataOrientation(count, event.sensor.getType(), event.accuracy,
               timeInMillis, false, dataToSend);

         if (getAbsoluteValues) {
            float[] mRotationMatrixGameCopy = mRotationMatrixGame.clone();
            System.out.println("GameRoatationMatrix" + Arrays.toString(mRotationMatrixGameCopy));
            float[] dataToSendAbsolute = new float[3];

            //            for (int i = 0; i < 3; i++) {
            //               dataToSendAbsolute[i] = (float) Math.toDegrees
            // (mRotationMatrixGameCopy[i]);
            //            }
            //            client.sendSensorDataOrientation(count, event.sensor.getType(), event
            // .accuracy,
            //                  timeInMillis, getAbsoluteValues, dataToSendAbsolute);
            client.sendSensorDataOrientation(count, event.sensor.getType(), event.accuracy,
                  timeInMillis, getAbsoluteValues, mRotationMatrixGameCopy);
         }
      }
   }

   private float getAngleChange360(int value, float angleDifference) {
      float lastValue;
      switch (value) {
         case 0:
            lastValue = lastValue1;
            break;
         case 1:
            lastValue = lastValue2;
            break;
         case 2:
            lastValue = lastValue3;
            break;
         default:
            lastValue = 0.0f;
      }
      if (Math.abs(lastValue - angleDifference) >= 300) {
         if (lastValue > 0 || (lastValue == 0 && Math.abs(angleDifference - 360.0) >= 360)) {
            angleDifference += 360.0;
         } else if (lastValue < 0) {
            angleDifference -= 360.0;
         }
      }
      return angleDifference;
   }

   @Override
   public void onAccuracyChanged(Sensor sensor, int accuracy) {

   }
}
