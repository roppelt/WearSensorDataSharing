package com.biankaroppelt.masterthesis;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;

import java.util.Arrays;
import java.util.Date;

public class SensorServiceAccelerometerGyroscope extends Service implements SensorEventListener {
   private static final String TAG = SensorServiceAccelerometerGyroscope.class.getName();

   private final static int SENS_ACCELEROMETER = Sensor.TYPE_ACCELEROMETER;
   private final static int SENS_GYROSCOPE = Sensor.TYPE_GYROSCOPE;
   private final static int SENS_GAME_ROTATION_VECTOR = Sensor.TYPE_GAME_ROTATION_VECTOR;

   SensorManager mSensorManager;
   private DeviceClient client;
   private int count = 0;

   public SensorServiceAccelerometerGyroscope() {
   }

   @Override
   public void onCreate() {
      super.onCreate();
      client = DeviceClient.getInstance(this);
      startMeasurementAccelerometerGyroscope();
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

   protected void startMeasurementAccelerometerGyroscope() {
      count = 0;
      mSensorManager = ((SensorManager) getSystemService(SENSOR_SERVICE));

      Sensor accelerometerSensor = mSensorManager.getDefaultSensor(SENS_ACCELEROMETER);
      //      Sensor gyroscopeSensor = mSensorManager.getDefaultSensor(SENS_GYROSCOPE);

      mSensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_GAME,
            SensorManager.SENSOR_DELAY_UI);
      //      mSensorManager.registerListener(this, gyroscopeSensor, SensorManager
      // .SENSOR_DELAY_NORMAL,
      //            SensorManager.SENSOR_DELAY_UI);
   }

   private void stopMeasurementOrientation() {
      if (mSensorManager != null) {
         mSensorManager.unregisterListener(this);
      }
   }

   @Override
   public void onSensorChanged(android.hardware.SensorEvent event) {
      count++;
      System.out.println("onSensorChanged: " + count + " - " + event.sensor.getName() + " - " +
            Arrays.toString(event.values));
      if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
         long timeInMillis =
               (new Date()).getTime() + (event.timestamp - System.nanoTime()) / 1000000L;
         float[] dataToSend = event.values.clone();
         client.sendSensorDataAccelerometerGyroscope(count, event.sensor.getType(), event.accuracy,
               timeInMillis, true, dataToSend);
      }
      //      else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
      //         long timeInMillis =
      //               (new Date()).getTime() + (event.timestamp - System.nanoTime()) / 1000000L;
      //         float[] dataToSend = event.values.clone();
      //         client.sendSensorDataAccelerometerGyroscope(event.sensor.getType(), event
      // .accuracy, timeInMillis,
      //               dataToSend);
      //      }
   }

   @Override
   public void onAccuracyChanged(Sensor sensor, int accuracy) {

   }
}
