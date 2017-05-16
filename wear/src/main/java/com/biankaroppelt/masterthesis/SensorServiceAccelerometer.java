package com.biankaroppelt.masterthesis;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;

import java.util.Arrays;
import java.util.Date;

public class SensorServiceAccelerometer extends Service implements SensorEventListener {
   private final static int SENSOR_ACCELEROMETER = Sensor.TYPE_ACCELEROMETER;
   private static final String TAG = SensorServiceAccelerometer.class.getName();
   private DeviceClient client;
   private int count = 0;
   private SensorManager sensorManager;

   public SensorServiceAccelerometer() {
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
      startMeasurementAccelerometer();
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
      if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
         count++;
         long timeInMillis =
               (new Date()).getTime() + (event.timestamp - System.nanoTime()) / 1000000L;
         float[] dataToSend = event.values.clone();
         client.sendSensorDataAccelerometer(count, event.sensor.getType(), event.accuracy,
               timeInMillis, true, dataToSend);
      }
   }

   protected void startMeasurementAccelerometer() {
      count = 0;
      sensorManager = ((SensorManager) getSystemService(SENSOR_SERVICE));
      Sensor accelerometerSensor = sensorManager.getDefaultSensor(SENSOR_ACCELEROMETER);
      sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_GAME,
            SensorManager.SENSOR_DELAY_UI);
   }

   private void stopMeasurementOrientation() {
      if (sensorManager != null) {
         sensorManager.unregisterListener(this);
      }
   }
}
