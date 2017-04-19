package com.biankaroppelt.masterthesis;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import java.util.Arrays;
import java.util.Date;

public class SensorServiceOrientation extends Service implements SensorEventListener {
   private static final String TAG = SensorServiceOrientation.class.getName();

   private final static int SENS_ACCELEROMETER = Sensor.TYPE_ACCELEROMETER;
   private final static int SENS_GYROSCOPE = Sensor.TYPE_GYROSCOPE;
   private final static int SENS_GAME_ROTATION_VECTOR = Sensor.TYPE_GAME_ROTATION_VECTOR;

   SensorManager mSensorManager;

   private DeviceClient client;
   private final float[] mAngleChanges = new float[9];
   private float[] mStartValuesGameRotation;
   private final float[] mRotationMatrixGame = new float[9];
   private float[] mRotationMatrixGameStart;

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
      Bundle extras = intent.getExtras();

      if(extras == null) {
         Log.d("Service","null");
      } else {
         Log.d("Service","not null");
          getAbsoluteValues = extras.getBoolean("absolute");
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

   @Override
   public void onSensorChanged(android.hardware.SensorEvent event) {

      if (event.sensor.getType() == Sensor.TYPE_GAME_ROTATION_VECTOR) {
         count++;
         System.out.println("onSensorChanged: " + count + " - " + event.sensor.getName() + " - " +
               Arrays.toString(event.values));

         SensorManager.getRotationMatrixFromVector(mRotationMatrixGame, event.values);

         if (mRotationMatrixGameStart == null) {
            mRotationMatrixGameStart = mRotationMatrixGame.clone();
         }

         SensorManager.getAngleChange(mAngleChanges, mRotationMatrixGame, mRotationMatrixGameStart);
         float[] dataToSend = new float[3];
         float[] angleChangesCopy = mAngleChanges.clone();

         for (int i = 0; i < 3; i++) {
            dataToSend[i] = (float) Math.toDegrees(angleChangesCopy[i]);
         }

         System.out.println(Arrays.toString(dataToSend));

         long timeInMillis =
               (new Date()).getTime() + (event.timestamp - System.nanoTime()) / 1000000L;

         client.sendSensorDataOrientation(count, event.sensor.getType(), event.accuracy,
               timeInMillis, false, dataToSend);

         if(getAbsoluteValues) {
            float[] dataToSendAbsolute = new float[3];
            float[] mRotationMatrixGameCopy = mRotationMatrixGame.clone();

            for (int i = 0; i < 3; i++) {
               dataToSendAbsolute[i] = (float) Math.toDegrees(mRotationMatrixGameCopy[i]);
            }
            client.sendSensorDataOrientation(count, event.sensor.getType(), event.accuracy,
                  timeInMillis, getAbsoluteValues, dataToSendAbsolute);
         }
      }
   }

   @Override
   public void onAccuracyChanged(Sensor sensor, int accuracy) {

   }
}
