package com.biankaroppelt.masterthesis;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.biankaroppelt.masterthesis.data.SensorDataPoint;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class SensorServiceOrientation extends Service implements SensorEventListener {
   private static final String TAG = SensorServiceOrientation.class.getName();

   private final static int SENS_ACCELEROMETER = Sensor.TYPE_ACCELEROMETER;
   private final static int SENS_MAGNETIC_FIELD = Sensor.TYPE_MAGNETIC_FIELD;
   private final static int SENS_GYROSCOPE = Sensor.TYPE_GYROSCOPE;
   private final static int SENS_ROTATION_VECTOR = Sensor.TYPE_ROTATION_VECTOR;
   private final static int SENS_MAGNETIC_FIELD_UNCALIBRATED =
         Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED;
   private final static int SENS_GAME_ROTATION_VECTOR = Sensor.TYPE_GAME_ROTATION_VECTOR;

   SensorManager mSensorManager;

   private DeviceClient client;
   private final float[] mAccelerometerReading = new float[3];
   private final float[] mMagnetometerReading = new float[3];

   private final float[] mRotationMatrix = new float[9];
   private float[] firstRotationMatrix = new float[9];
   private final float[] mOrientationAngles = new float[3];
   private final float[] mAngleChanges = new float[9];
   private float[] mStartValuesGameRotation;
   private final float[] mRotationMatrixGame = new float[9];
   private float[] mRotationMatrixGameStart;

   private boolean acc = false;
   private boolean mag = false;

   private ExecutorService executor;
   private ArrayList<SensorDataPoint> sensorDataPoints;
   private Handler handler;
   private Runnable mRunnable;
   private WebSocketClient mWebSocketClient;
   private SendDataToServletTask sendDataAsyncTask;
   private Socket socket;

   public SensorServiceOrientation() {
   }

   @Override
   public void onCreate() {
      super.onCreate();

      client = DeviceClient.getInstance(this);

      startMeasurementOrientation();
      executor = Executors.newFixedThreadPool(1000);
      sensorDataPoints = new ArrayList<>();
      sendDataAsyncTask = new SendDataToServletTask();
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
      mSensorManager = ((SensorManager) getSystemService(SENSOR_SERVICE));

      Sensor accelerometerSensor = mSensorManager.getDefaultSensor(SENS_ACCELEROMETER);
      Sensor magneticFieldSensor = mSensorManager.getDefaultSensor(SENS_MAGNETIC_FIELD);
      Sensor gameRotationVectorSensor = mSensorManager.getDefaultSensor(SENS_GAME_ROTATION_VECTOR);

//      mSensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL,
//            SensorManager.SENSOR_DELAY_UI);
//      mSensorManager.registerListener(this, magneticFieldSensor, SensorManager.SENSOR_DELAY_NORMAL,
//            SensorManager.SENSOR_DELAY_UI);
//      mSensorManager.registerListener(this, gameRotationVectorSensor,
//            SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);
      mSensorManager.registerListener(this, gameRotationVectorSensor,
            SensorManager.SENSOR_DELAY_GAME, SensorManager.SENSOR_DELAY_UI);
   }

   private void stopMeasurementOrientation() {
      if (mSensorManager != null) {
         mSensorManager.unregisterListener(this);
      }
      if(handler != null) {
         handler.removeCallbacks(mRunnable);
      }
   }

   @Override
   public void onSensorChanged(android.hardware.SensorEvent event) {

      if (event.sensor.getType() == Sensor.TYPE_GAME_ROTATION_VECTOR) {

         SensorManager.getRotationMatrixFromVector(
               mRotationMatrixGame , event.values);

         if (mRotationMatrixGameStart == null) {
            mRotationMatrixGameStart = mRotationMatrixGame.clone();
         }

         SensorManager.getAngleChange(mAngleChanges, mRotationMatrixGame, mRotationMatrixGameStart);
         String sysOut = "Values in Degrees: ";
//         String sysOutRelative = "Values relative: ";
         float[] dataToSend = new float[3];
         float[] angleChangesCopy = mAngleChanges.clone();

         for(int i = 0; i < 3; i++) {
            dataToSend[i] = (float) Math.toDegrees(angleChangesCopy[i]);
         }
//         for (float value : mAngleChanges) {
//            sysOut += (Math.toDegrees(value)) + " | ";
//         }
//         System.out.println(sysOut);

//         float[] values = event.values.clone();
//         if (mStartValuesGameRotation == null) {
//            mStartValuesGameRotation = values;
//         }
         long timeInMillis =
               (new Date()).getTime() + (event.timestamp - System.nanoTime()) / 1000000L;

//         String sysOut = "Values in Degrees: ";
//         String sysOutRelative = "Values relative: ";
//         int index = 0;
//         for (float value : values) {
//            sysOut += (Math.toDegrees(value)) + " | ";
//
//            double phi = Math.abs(
//                  (Math.toDegrees(value) - Math.toDegrees(mStartValuesGameRotation[index]))) %
//                        360;       // This is either the distance or 360 - distance
//            double distance = phi > 180 ? 360 - phi : phi;
//            sysOutRelative += (distance) + " | ";
//            index++;
//         } System.out.println(sysOut);
//         System.out.println(sysOutRelative);

         client.sendSensorDataOrientation(event.sensor.getType(), event.accuracy, timeInMillis,
               dataToSend);

         SensorDataPoint dataPoint = new SensorDataPoint(event.sensor, timeInMillis, event.accuracy, dataToSend);
//         sendDataPointToServlet(dataPoint);
//         addDataPointToList(dataPoint);
//         if(mWebSocketClient == null) {
//            connectWebSocket();
//         }
         return;
      } else if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
         System.arraycopy(event.values, 0, mAccelerometerReading, 0, mAccelerometerReading.length);
         acc = true;
      } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
         System.arraycopy(event.values, 0, mMagnetometerReading, 0, mMagnetometerReading.length);
         mag = true;
      } if (acc && mag) {
         updateOrientationAngles();
      }
//      if(handler == null) {
//         registerHandler();
//      }
   }

   private void addDataPointToList(SensorDataPoint sensorDataPoint) {
      sensorDataPoints.add(sensorDataPoint);
   }

   private void registerHandler() {
      handler = new Handler();
      mRunnable = new Runnable() {
         @Override
         public void run() {
            sendDataPointsToServlet();
            handler.postDelayed(this, 1000);
         }
      };
      handler.postDelayed(mRunnable, 1000);

//      final int delay = 1000; //milliseconds
//
//      handler.postDelayed(new Runnable(){
//         public void run(){
//            //do something
//            sendDataPointsToServlet();
//            handler.postDelayed(this, delay);
//         }
//      }, delay);

   }

   private void sendDataPointsToServlet() {
      ConnectivityManager connMgr =
            (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
      NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
      if (networkInfo != null && networkInfo.isConnected()) {
         // fetch data
         String stringUrl = "http://master.localtunnel.me/html/wsserver/client_send.php";
         sendDataAsyncTask.setData(stringUrl, sensorDataPoints);
         executor.execute(sendDataAsyncTask);
         System.gc();
         sensorDataPoints.clear();
      } else {
         System.out.println("ERROR");
      }
   }

//   private void sendDataPointToServlet(SensorDataPoint data) {
//      ConnectivityManager connMgr =
//            (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
//      NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
//      if (networkInfo != null && networkInfo.isConnected()) {
//         // fetch data
//         String stringUrl = "http://master.localtunnel.me/html/wsserver/client_send.php";
////         if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ) {
////            new SendDataToServletTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, stringUrl, data);
////         } else {
////            new SendDataToServletTask().execute(stringUrl, data);
////         }
//         executor.execute(new SendDataToServletTask(stringUrl, data));
//         System.gc();
//      } else {
//         System.out.println("ERROR");
//      }
//   }


   // Compute the three orientation angles based on the most recent readings from
   // the device's accelerometer and magnetometer.
   public void updateOrientationAngles() {
      // Update rotation matrix, which is needed to update orientation angles.
      if (SensorManager.getRotationMatrix(mRotationMatrix, null, mAccelerometerReading,
            mMagnetometerReading)) {

         float[] temp = new float[9];
         SensorManager.remapCoordinateSystem(mRotationMatrix, SensorManager.AXIS_X,
               SensorManager.AXIS_Z, temp);
         // "mRotationMatrix" now has up-to-date information.

         SensorManager.getOrientation(temp, mOrientationAngles);

         // "mOrientationAngles" now has up-to-date information.

         //         String sysOut = "Values in Degrees: ";
         //         for (float value : mOrientationAngles) {
         //            sysOut += (Math.toDegrees(value)) + " | ";
         //         }
         //         System.out.println(sysOut);
         for (int i = 0; i < mOrientationAngles.length; i++) {
            Double degrees = (mOrientationAngles[i] * 180) / Math.PI;
            mOrientationAngles[i] = degrees.floatValue();
         }

         if (Float.isNaN(firstRotationMatrix[0]) || firstRotationMatrix[0] == 0.0) {
            firstRotationMatrix = mRotationMatrix.clone();
         }

         //         SensorManager.getAngleChange(mAngleChanges, mRotationMatrix,
         // firstRotationMatrix);
         //
         //         String sysOut = "Values in Degrees: ";
         //         for (float value : mAngleChanges) {
         //            sysOut += (Math.toDegrees(value)) + " | ";
         //         }
         //         System.out.println(sysOut);

         //         System.out.println(Arrays.toString(mOrientationAngles));

      }
   }

   @Override
   public void onAccuracyChanged(Sensor sensor, int accuracy) {

   }

   private void connectWebSocket() {

      URI uri;
      uri = URI.create("ws://10.176.94.16:1234/");

      mWebSocketClient = new WebSocketClient(uri) {
         @Override
         public void onOpen(ServerHandshake serverHandshake) {
            Log.i("Websocket", "Opened");
            System.out.println("websocket open");
            mWebSocketClient.send("Hello from " + Build.MANUFACTURER + " " + Build.MODEL);
         }

         @Override
         public void onMessage(String s) {
            final String message = s;
            new Runnable() {
               @Override
               public void run() {
                  System.out.println("NEW MESSAGE: " + message);
               }
            }.run();
         }

         @Override
         public void onClose(int i, String s, boolean b) {
            Log.i("Websocket", "Closed " + s);
         }

         @Override
         public void onError(Exception e) {
            Log.i("Websocket", "Error " + e.getMessage());
         }
      };
      mWebSocketClient.connect();
   }
}
