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
   private final static int SENS_GAME_ROTATION_VECTOR = Sensor.TYPE_GAME_ROTATION_VECTOR;
   private static final String TAG = SensorServiceOrientation.class.getSimpleName();
   private final float[] angleChanges = new float[9];
   private final float[] rotationMatrixGame = new float[9];
   private DeviceClient client;
   private int count = 0;
   private boolean getAbsoluteValues = false;
   private float lastValue1 = 0.0f;
   private float lastValue2 = 0.0f;
   private float lastValue3 = 0.0f;
   private float[] rotationMatrixGameStart;
   private SensorManager sensorManager;

   public SensorServiceOrientation() {
   }

   @Override
   public void onAccuracyChanged(Sensor sensor, int accuracy) {

   }

   @Override
   public IBinder onBind(Intent intent) {
      return null;
   }

   @Override
   public void onCreate() {
      super.onCreate();
      client = DeviceClient.getInstance(this);
      startMeasurementOrientation();
   }

   @Override
   public void onDestroy() {
      super.onDestroy();
      stopMeasurementOrientation();
   }

   @Override
   public void onSensorChanged(android.hardware.SensorEvent event) {
      Log.d(TAG, "onSensorChanged: " + count + " - " + event.sensor.getName() + " - " +
            Arrays.toString(event.values));
      if (event.sensor.getType() == Sensor.TYPE_GAME_ROTATION_VECTOR) {
         count++;

         SensorManager.getRotationMatrixFromVector(rotationMatrixGame, event.values.clone());
         if (rotationMatrixGameStart == null) {
            rotationMatrixGameStart = rotationMatrixGame.clone();
         }

         SensorManager.getAngleChange(angleChanges, rotationMatrixGame, rotationMatrixGameStart);

         float[] dataToSend = new float[3];
         float[] angleChangesCopy = angleChanges.clone();

         // Rotate
         lastValue1 = getAngleChange360(0, (float) Math.toDegrees(angleChangesCopy[0]));
         dataToSend[0] = lastValue1;

         // Lift
         lastValue3 = getAngleChange360(2, (float) Math.toDegrees(angleChangesCopy[2]));
         dataToSend[2] = lastValue3;

         // Roll
         lastValue2 = getAngleChange360(1, (float) (
               Math.toDegrees(Math.atan2(rotationMatrixGameStart[7], rotationMatrixGameStart[8])) -
                     Math.toDegrees(Math.atan2(rotationMatrixGame[7], rotationMatrixGame[8]))));
         dataToSend[1] = lastValue2;

         long timeInMillis = (System.currentTimeMillis() +
               Math.round((event.timestamp - SystemClock.elapsedRealtimeNanos()) / 1000000L));

         client.sendSensorDataOrientation(count, event.sensor.getType(), event.accuracy,
               timeInMillis, false, dataToSend);

         if (getAbsoluteValues) {
            float[] mRotationMatrixGameCopy = rotationMatrixGame.clone();
            client.sendSensorDataOrientation(count, event.sensor.getType(), event.accuracy,
                  timeInMillis, getAbsoluteValues, mRotationMatrixGameCopy);
         }
      }
   }

   @Override
   public void onStart(Intent intent, int startId) {
      super.onStart(intent, startId);
      if (intent != null) {
         Bundle extras = intent.getExtras();
         if (extras != null) {
            getAbsoluteValues = extras.getBoolean("absolute");
         }
      }
   }

   protected void startMeasurementOrientation() {
      count = 0;
      sensorManager = ((SensorManager) getSystemService(SENSOR_SERVICE));
      Sensor gameRotationVectorSensor = sensorManager.getDefaultSensor(SENS_GAME_ROTATION_VECTOR);
      sensorManager.registerListener(this, gameRotationVectorSensor,
            SensorManager.SENSOR_DELAY_GAME, SensorManager.SENSOR_DELAY_UI);
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
         if (lastValue > 0) {
            angleDifference += 360.0;
         } else if (lastValue < 0) {
            angleDifference -= 360.0;
         }
      }
      return angleDifference;
   }

   private void stopMeasurementOrientation() {
      if (sensorManager != null) {
         sensorManager.unregisterListener(this);
      }
   }
}
