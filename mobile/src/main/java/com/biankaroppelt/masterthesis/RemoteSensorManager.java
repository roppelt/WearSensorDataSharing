package com.biankaroppelt.masterthesis;

import android.content.Context;
import android.util.Log;
import android.util.SparseArray;

import com.biankaroppelt.datalogger.ClientPaths;
import com.biankaroppelt.datalogger.DataMapKeys;
import com.biankaroppelt.masterthesis.data.Sensor;
import com.biankaroppelt.masterthesis.data.SensorDataPoint;
import com.biankaroppelt.masterthesis.data.SensorNames;
import com.biankaroppelt.masterthesis.events.BusProvider;
import com.biankaroppelt.masterthesis.events.NewSensorEvent;
import com.biankaroppelt.masterthesis.events.NoNodesAvailableEvent;
import com.biankaroppelt.masterthesis.events.SensorUpdatedEvent;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class RemoteSensorManager {
   private static final String TAG = "RemoteSensorManager";
   private static final int CLIENT_CONNECTION_TIMEOUT = 15000;

   private static RemoteSensorManager instance;

   private Context context;
   private ExecutorService executorService;
   private SparseArray<Sensor> sensorMapping;
   private ArrayList<Sensor> sensors;
   private SensorNames sensorNames;
   private GoogleApiClient googleApiClient;

   public static synchronized RemoteSensorManager getInstance(Context context) {
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

   public List<Sensor> getSensors() {
      return (List<Sensor>) sensors.clone();
   }

   public Sensor getSensor(long id) {
      return sensorMapping.get((int) id);
   }

   private Sensor createSensor(int id) {
      Sensor sensor = new Sensor(id, sensorNames.getName(id));

      sensors.add(sensor);
      sensorMapping.append(id, sensor);

      BusProvider.postOnMainThread(new NewSensorEvent(sensor));

      return sensor;
   }

   private Sensor getOrCreateSensor(int id) {
      Sensor sensor = sensorMapping.get(id);

      if (sensor == null) {
         sensor = createSensor(id);
      }

      return sensor;
   }

   public synchronized void addSensorData(ArrayList<DataMap> list) {
      ArrayList<SensorDataPoint> sensorDataPointList = new ArrayList<>();
      for (DataMap element : list) {
         int sensorType = element.getInt(DataMapKeys.SENSOR_TYPE);
         int accuracy = element.getInt(DataMapKeys.ACCURACY);
         long timestamp = element.getLong(DataMapKeys.TIMESTAMP);
         float[] values = element.getFloatArray(DataMapKeys.VALUES);

         Sensor sensor = getOrCreateSensor(sensorType);

         // TODO: We probably want to pull sensor data point objects from a pool here
         SensorDataPoint dataPoint = new SensorDataPoint(sensor, timestamp, accuracy, values);

         sensor.addDataPoint(dataPoint);
         sensorDataPointList.add(dataPoint);
      }
      BusProvider.postOnMainThread(new SensorUpdatedEvent(sensorDataPointList));
   }

   private boolean validateConnection() {
      if (googleApiClient.isConnected()) {
         return true;
      }

      ConnectionResult result =
            googleApiClient.blockingConnect(CLIENT_CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS);

      return result.isSuccess();
   }

   public void startMeasurement() {
      executorService.submit(new Runnable() {
         @Override
         public void run() {
            controlMeasurementInBackground(ClientPaths.START_MEASUREMENT);
         }
      });
   }

   public void stopMeasurement() {
      executorService.submit(new Runnable() {
         @Override
         public void run() {
            controlMeasurementInBackground(ClientPaths.STOP_MEASUREMENT);
         }
      });
   }

   private void controlMeasurementInBackground(final String path) {
      if (validateConnection()) {
         List<Node> nodes = Wearable.NodeApi.getConnectedNodes(googleApiClient)
               .await()
               .getNodes();
         Log.d(TAG, "Sending to nodes: " + nodes.size());

         if(nodes.isEmpty()) {
            BusProvider.postOnMainThread(new NoNodesAvailableEvent());
         } else {

            for (Node node : nodes) {
               Log.i(TAG, "add node " + node.getDisplayName());
               Wearable.MessageApi.sendMessage(googleApiClient, node.getId(), path, null)
                     .setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
                        @Override
                        public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                           //                        Log.d(TAG, "controlMeasurementInBackground(" + path + "): " +
                           //                              sendMessageResult.getStatus()
                           //                                    .isSuccess());
                        }
                     });
            }
         }

      } else {
         Log.w(TAG, "No connection possible");
      }
   }
}

