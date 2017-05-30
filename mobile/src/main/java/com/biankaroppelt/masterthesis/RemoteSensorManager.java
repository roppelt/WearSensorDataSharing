package com.biankaroppelt.masterthesis;

import android.content.Context;
import android.util.Log;
import android.util.SparseArray;

import com.biankaroppelt.datalogger.SharedStrings;
import com.biankaroppelt.masterthesis.data.Sensor;
import com.biankaroppelt.masterthesis.data.SensorDataPoint;
import com.biankaroppelt.masterthesis.data.SensorNames;
import com.biankaroppelt.masterthesis.events.BusProvider;
import com.biankaroppelt.masterthesis.events.NoNodesAvailableEvent;
import com.biankaroppelt.masterthesis.events.SensorUpdatedEvent;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

class RemoteSensorManager {
   private static final int CLIENT_CONNECTION_TIMEOUT = 15000;
   private static final String TAG = RemoteSensorManager.class.getSimpleName();
   private static RemoteSensorManager instance;

   private Context context;
   private ExecutorService executorService;
   private GoogleApiClient googleApiClient;
   private SparseArray<Sensor> sensorMapping;
   private SensorNames sensorNames;
   private ArrayList<Sensor> sensors;

   static synchronized RemoteSensorManager getInstance(Context context) {
      if (instance == null) {
         instance = new RemoteSensorManager(context.getApplicationContext());
      }
      return instance;
   }

   private RemoteSensorManager(Context context) {
      this.context = context;
      this.sensorMapping = new SparseArray<>();
      this.sensors = new ArrayList<>();
      this.sensorNames = new SensorNames();
      this.googleApiClient = new GoogleApiClient.Builder(context).addApi(Wearable.API)
            .build();
      this.executorService = Executors.newCachedThreadPool();
   }

   public Sensor getSensor(long id) {
      return sensorMapping.get((int) id);
   }

   synchronized void addSensorData(String data) {
      ArrayList<SensorDataPoint> sensorDataPointList = new ArrayList<>();
      String[] dataList = data.split(";");
      if (dataList.length > 0) {
         int index = Integer.parseInt(dataList[0]);
         int sensorType = Integer.parseInt(dataList[1]);
         int accuracy = Integer.parseInt(dataList[2]);
         long timestamp = Long.parseLong(dataList[3]);
         boolean absolute = Boolean.parseBoolean(dataList[4]);
         float[] values = parseStringAsList(dataList[5]);

         Sensor sensor = getOrCreateSensor(sensorType);
         SensorDataPoint dataPoint =
               new SensorDataPoint(sensor, timestamp, accuracy, absolute, values);

         sensor.addDataPoint(dataPoint);
         sensorDataPointList.add(dataPoint);
         BusProvider.postOnMainThread(new SensorUpdatedEvent(sensorDataPointList));
      }
   }

   void startMeasurementAccelerometerPilotStudy1B() {
      executorService.submit(new Runnable() {
         @Override
         public void run() {
            controlMeasurementInBackground(
                  SharedStrings.START_MEASUREMENT_ACCELEROMETER_PILOT_STUDY_1B);
         }
      });
   }

   void startMeasurementOrientationMainStudy() {
      executorService.submit(new Runnable() {
         @Override
         public void run() {
            controlMeasurementInBackground(SharedStrings.START_MEASUREMENT_ORIENTATION_MAIN_STUDY);
         }
      });
   }

   void startMeasurementOrientationPilotStudy1A() {
      executorService.submit(new Runnable() {
         @Override
         public void run() {
            controlMeasurementInBackground(
                  SharedStrings.START_MEASUREMENT_ORIENTATION_PILOT_STUDY_1A);
         }
      });
   }

   void startMeasurementOrientationPilotStudy2() {
      executorService.submit(new Runnable() {
         @Override
         public void run() {
            controlMeasurementInBackground(SharedStrings.START_MEASUREMENT_ORIENTATION_PILOT_STUDY_2);
         }
      });
   }

   void stopMeasurementAccelerometerPilotStudy1B() {
      executorService.submit(new Runnable() {
         @Override
         public void run() {
            controlMeasurementInBackground(
                  SharedStrings.STOP_MEASUREMENT_ACCELEROMETER_PILOT_STUDY_1B);
         }
      });
   }

   void stopMeasurementOrientationMainStudy() {
      executorService.submit(new Runnable() {
         @Override
         public void run() {
            controlMeasurementInBackground(SharedStrings.STOP_MEASUREMENT_ORIENTATION_MAIN_STUDY);
         }
      });
   }

   void stopMeasurementOrientationPilotStudy1A() {
      executorService.submit(new Runnable() {
         @Override
         public void run() {
            controlMeasurementInBackground(SharedStrings.STOP_MEASUREMENT_ORIENTATION_PILOT_STUDY_1A);
         }
      });
   }

   void stopMeasurementOrientationPilotStudy2() {
      executorService.submit(new Runnable() {
         @Override
         public void run() {
            controlMeasurementInBackground(SharedStrings.STOP_MEASUREMENT_ORIENTATION_PILOT_STUDY_2);
         }
      });
   }

   private void controlMeasurementInBackground(final String path) {
      if (validateConnection()) {
         List<Node> nodes = Wearable.NodeApi.getConnectedNodes(googleApiClient)
               .await()
               .getNodes();
         Log.d(TAG, "Sending to nodes: " + nodes.size());
         if (nodes.isEmpty() || nodes.size() == 0) {
            BusProvider.postOnMainThread(new NoNodesAvailableEvent());
         } else {
            for (Node node : nodes) {
               Log.d(TAG, "add node " + node.getDisplayName());
               Wearable.MessageApi.sendMessage(googleApiClient, node.getId(), path, null);
            }
         }
      } else {
         Log.d(TAG, "No connection possible");
      }
   }

   private Sensor createSensor(int id) {
      Sensor sensor = new Sensor(id, sensorNames.getName(id));
      sensors.add(sensor);
      sensorMapping.append(id, sensor);
      return sensor;
   }

   private Sensor getOrCreateSensor(int id) {
      Sensor sensor = sensorMapping.get(id);
      if (sensor == null) {
         sensor = createSensor(id);
      }
      return sensor;
   }

   private float[] parseStringAsList(String s) {
      String[] listString = s.substring(1, s.length() - 1)
            .split(",");
      float[] output = new float[listString.length];
      for (int i = 0; i < listString.length; i++) {
         output[i] = Float.parseFloat(listString[i]);
      }
      return output;
   }

   private boolean validateConnection() {
      if (googleApiClient.isConnected()) {
         return true;
      }
      ConnectionResult result =
            googleApiClient.blockingConnect(CLIENT_CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS);
      return result.isSuccess();
   }
}

