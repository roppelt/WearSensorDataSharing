package com.biankaroppelt.masterthesis;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.IBinder;

import com.biankaroppelt.masterthesis.data.SensorDataPoint;
import com.biankaroppelt.masterthesis.events.SensorEvent;

import java.util.Date;

public class SensorService extends Service implements SensorEventListener {
   private static final String TAG = SensorService.class.getName();

   private final static int SENS_ACCELEROMETER = Sensor.TYPE_ACCELEROMETER;
   private final static int SENS_MAGNETIC_FIELD = Sensor.TYPE_MAGNETIC_FIELD;
   // 3 = @Deprecated Orientation
   private final static int SENS_GYROSCOPE = Sensor.TYPE_GYROSCOPE;
   private final static int SENS_LIGHT = Sensor.TYPE_LIGHT;
   private final static int SENS_PRESSURE = Sensor.TYPE_PRESSURE;
   // 7 = @Deprecated Temperature
   private final static int SENS_PROXIMITY = Sensor.TYPE_PROXIMITY;
   private final static int SENS_GRAVITY = Sensor.TYPE_GRAVITY;
   private final static int SENS_LINEAR_ACCELERATION = Sensor.TYPE_LINEAR_ACCELERATION;
   private final static int SENS_ROTATION_VECTOR = Sensor.TYPE_ROTATION_VECTOR;
   private final static int SENS_HUMIDITY = Sensor.TYPE_RELATIVE_HUMIDITY;
   // TODO: there's no Android Wear devices yet with a body temperature monitor
   private final static int SENS_AMBIENT_TEMPERATURE = Sensor.TYPE_AMBIENT_TEMPERATURE;
   private final static int SENS_MAGNETIC_FIELD_UNCALIBRATED =
         Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED;
   private final static int SENS_GAME_ROTATION_VECTOR = Sensor.TYPE_GAME_ROTATION_VECTOR;
   private final static int SENS_GYROSCOPE_UNCALIBRATED = Sensor.TYPE_GYROSCOPE_UNCALIBRATED;
   private final static int SENS_SIGNIFICANT_MOTION = Sensor.TYPE_SIGNIFICANT_MOTION;
   private final static int SENS_STEP_DETECTOR = Sensor.TYPE_STEP_DETECTOR;
   private final static int SENS_STEP_COUNTER = Sensor.TYPE_STEP_COUNTER;
   private final static int SENS_GEOMAGNETIC = Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR;
   private final static int SENS_HEARTRATE = Sensor.TYPE_HEART_RATE;

   //   private Sensor mHeartrateSensor;
   //   private ScheduledExecutorService mScheduler;

   SensorManager mSensorManager;

   private DeviceClient client;

   @Override
   public void onCreate() {
      super.onCreate();

      client = DeviceClient.getInstance(this);

      //      Notification.Builder builder = new Notification.Builder(this);
      //      builder.setContentTitle(getString(R.string.app_name));
      //      builder.setContentText("Collecting sensor data..");
      //      builder.setSmallIcon(R.mipmap.ic_launcher);

      //      startForeground(1, builder.build());

      startMeasurement();
   }

   @Override
   public void onDestroy() {
      super.onDestroy();

      stopMeasurement();
   }

   @Override
   public IBinder onBind(Intent intent) {
      return null;
   }

   protected void startMeasurement() {
      mSensorManager = ((SensorManager) getSystemService(SENSOR_SERVICE));

      Sensor accelerometerSensor = mSensorManager.getDefaultSensor(SENS_ACCELEROMETER);
      Sensor ambientTemperatureSensor = mSensorManager.getDefaultSensor(SENS_AMBIENT_TEMPERATURE);
      Sensor gameRotationVectorSensor = mSensorManager.getDefaultSensor(SENS_GAME_ROTATION_VECTOR);
      Sensor geomagneticSensor = mSensorManager.getDefaultSensor(SENS_GEOMAGNETIC);
      Sensor gravitySensor = mSensorManager.getDefaultSensor(SENS_GRAVITY);
      Sensor gyroscopeSensor = mSensorManager.getDefaultSensor(SENS_GYROSCOPE);
      Sensor gyroscopeUncalibratedSensor =
            mSensorManager.getDefaultSensor(SENS_GYROSCOPE_UNCALIBRATED);
      //      mHeartrateSensor = mSensorManager.getDefaultSensor(SENS_HEARTRATE);
      //      Sensor heartrateSamsungSensor = mSensorManager.getDefaultSensor(65562);
      Sensor lightSensor = mSensorManager.getDefaultSensor(SENS_LIGHT);
      Sensor linearAccelerationSensor = mSensorManager.getDefaultSensor(SENS_LINEAR_ACCELERATION);
      Sensor magneticFieldSensor = mSensorManager.getDefaultSensor(SENS_MAGNETIC_FIELD);
      Sensor magneticFieldUncalibratedSensor =
            mSensorManager.getDefaultSensor(SENS_MAGNETIC_FIELD_UNCALIBRATED);
      Sensor pressureSensor = mSensorManager.getDefaultSensor(SENS_PRESSURE);
      Sensor proximitySensor = mSensorManager.getDefaultSensor(SENS_PROXIMITY);
      Sensor humiditySensor = mSensorManager.getDefaultSensor(SENS_HUMIDITY);
      Sensor rotationVectorSensor = mSensorManager.getDefaultSensor(SENS_ROTATION_VECTOR);
      Sensor significantMotionSensor = mSensorManager.getDefaultSensor(SENS_SIGNIFICANT_MOTION);
      Sensor stepCounterSensor = mSensorManager.getDefaultSensor(SENS_STEP_COUNTER);
      Sensor stepDetectorSensor = mSensorManager.getDefaultSensor(SENS_STEP_DETECTOR);

      // Register the listener
      if (mSensorManager != null) {
         if (accelerometerSensor != null) {
            mSensorManager.registerListener(this, accelerometerSensor,
                  SensorManager.SENSOR_DELAY_NORMAL);
         }

         //         if (ambientTemperatureSensor != null) {
         //            mSensorManager.registerListener(this, ambientTemperatureSensor,
         //                  SensorManager.SENSOR_DELAY_NORMAL);
         //         }
         //
         //         if (gameRotationVectorSensor != null) {
         //            mSensorManager.registerListener(this, gameRotationVectorSensor,
         //                  SensorManager.SENSOR_DELAY_NORMAL);
         //         }
         //
         //         if (geomagneticSensor != null) {
         //            mSensorManager.registerListener(this, geomagneticSensor,
         //                  SensorManager.SENSOR_DELAY_NORMAL);
         //         }
         //
         //         if (gravitySensor != null) {
         //            mSensorManager.registerListener(this, gravitySensor, SensorManager
         // .SENSOR_DELAY_NORMAL);
         //         }
         //
         if (gyroscopeSensor != null) {
            mSensorManager.registerListener(this, gyroscopeSensor,
                  SensorManager.SENSOR_DELAY_NORMAL);
         }
         //
         //         if (gyroscopeUncalibratedSensor != null) {
         //            mSensorManager.registerListener(this, gyroscopeUncalibratedSensor,
         //                  SensorManager.SENSOR_DELAY_NORMAL);
         //         }
         //
         //         if (lightSensor != null) {
         //            mSensorManager.registerListener(this, lightSensor, SensorManager
         // .SENSOR_DELAY_NORMAL);
         //         }
         //
         //         if (linearAccelerationSensor != null) {
         //            mSensorManager.registerListener(this, linearAccelerationSensor,
         //                  SensorManager.SENSOR_DELAY_NORMAL);
         //         }
         //
         //         if (magneticFieldSensor != null) {
         //            mSensorManager.registerListener(this, magneticFieldSensor,
         //                  SensorManager.SENSOR_DELAY_NORMAL);
         //         }
         //
         //         if (magneticFieldUncalibratedSensor != null) {
         //            mSensorManager.registerListener(this, magneticFieldUncalibratedSensor,
         //                  SensorManager.SENSOR_DELAY_NORMAL);
         //         }
         //
         //         if (pressureSensor != null) {
         //            mSensorManager.registerListener(this, pressureSensor,
         //                  SensorManager.SENSOR_DELAY_NORMAL);
         //         }
         //
         //         if (proximitySensor != null) {
         //            mSensorManager.registerListener(this, proximitySensor,
         //                  SensorManager.SENSOR_DELAY_NORMAL);
         //         }
         //
         //         if (humiditySensor != null) {
         //            mSensorManager.registerListener(this, humiditySensor,
         //                  SensorManager.SENSOR_DELAY_NORMAL);
         //         }
         //
         //         if (rotationVectorSensor != null) {
         //            mSensorManager.registerListener(this, rotationVectorSensor,
         //                  SensorManager.SENSOR_DELAY_NORMAL);
         //         }
         //
         //         if (significantMotionSensor != null) {
         //            mSensorManager.registerListener(this, significantMotionSensor,
         //                  SensorManager.SENSOR_DELAY_NORMAL);
         //         }
         //
         //         if (stepCounterSensor != null) {
         //            mSensorManager.registerListener(this, stepCounterSensor,
         //                  SensorManager.SENSOR_DELAY_NORMAL);
         //         }
         //
         //         if (stepDetectorSensor != null) {
         //            mSensorManager.registerListener(this, stepDetectorSensor,
         //                  SensorManager.SENSOR_DELAY_NORMAL);
         //         }
      }
   }

   private void stopMeasurement() {
      if (mSensorManager != null) {
         mSensorManager.unregisterListener(this);
      }
      client.sendSensorData();
   }

   @Override
   public void onSensorChanged(android.hardware.SensorEvent event) {
      float[] valuesCopy = event.values.clone();
      long timeInMillis = (new Date()).getTime() + (event.timestamp - System.nanoTime()) / 1000000L;

      SensorEvent eventCopy =
            new SensorEvent(event.accuracy, event.sensor, event.timestamp, valuesCopy);
      client.addEventToList(eventCopy);
//      SensorDataPoint dataPoint = new SensorDataPoint(event.sensor, timeInMillis, event.accuracy, valuesCopy);
//      sendDataPointToServlet(dataPoint);
   }


   private void sendDataPointToServlet(SensorDataPoint data) {
      ConnectivityManager connMgr =
            (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
      NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
      if (networkInfo != null && networkInfo.isConnected()) {
         // fetch data
         String stringUrl = "http://master.localtunnel.me/html/wsserver/client_send.php";
//         new SendDataToServletTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, stringUrl, data);
      } else {
         System.out.println("ERROR");
      }
   }

   @Override
   public void onAccuracyChanged(Sensor sensor, int accuracy) {

   }
}
