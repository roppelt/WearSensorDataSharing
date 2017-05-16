package com.biankaroppelt.masterthesis;

import android.content.Context;
import android.util.Log;
import android.util.SparseLongArray;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

class DeviceClient {
   private static final int CLIENT_CONNECTION_TIMEOUT = 15000;
   private static final String TAG = DeviceClient.class.getName();
   private static DeviceClient instance;
   private GoogleApiClient googleApiClient;
   private SparseLongArray lastSensorData;
   private List<Node> nodes;

   static DeviceClient getInstance(Context context) {
      if (instance == null) {
         instance = new DeviceClient(context.getApplicationContext());
      }
      return instance;
   }

   private DeviceClient(Context context) {
      googleApiClient = new GoogleApiClient.Builder(context).addApi(Wearable.API)
            .build();
      lastSensorData = new SparseLongArray();
   }

   void sendSensorDataAccelerometer(final int index, final int sensorType, final int accuracy,
         final long timestamp, boolean absolute, final float[] values) {
      long t = System.currentTimeMillis();
      final String data =
            index + ";" + sensorType + ";" + accuracy + ";" + timestamp + ";" + absolute + ";" +
                  Arrays.toString(values);
      lastSensorData.put(sensorType, t);
      sendMessage("/sensorsAccelerometer/", data);
   }

   void sendSensorDataOrientation(final int index, final int sensorType, final int accuracy,
         final long timestamp, boolean absolute, final float[] values) {
      long t = System.currentTimeMillis();
      final String data =
            index + ";" + sensorType + ";" + accuracy + ";" + timestamp + ";" + absolute + ";" +
                  Arrays.toString(values);
      lastSensorData.put(sensorType, t);
      sendMessage("/sensorsOrientation/", data);
   }

   private void sendMessage(final String path, final String data) {
      if (validateConnection()) {
         if (nodes == null || nodes.isEmpty()) {
            Log.e(TAG, "NO NODE");
            new Thread(new Runnable() {
               @Override
               public void run() {
                  googleApiClient.blockingConnect(CLIENT_CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS);
                  nodes = Wearable.NodeApi.getConnectedNodes(googleApiClient)
                        .await()
                        .getNodes();
                  sendMessageToNodes(path, data);
               }
            }).start();
         } else {
            for (Node node : nodes) {
               Log.d(TAG, "add node " + node);
               sendMessageToNodes(path, data);
            }
         }
      } else {
         Log.d(TAG, "No connection possible");
      }
   }

   private void sendMessageToNodes(final String path, String data) {
      for (Node node : nodes) {
         Wearable.MessageApi.sendMessage(googleApiClient, node.getId(), path, data.getBytes());
      }
   }

   private boolean validateConnection() {
      if (googleApiClient.isConnected()) {
         return true;
      }
      new Thread(new Runnable() {
         @Override
         public void run() {
            googleApiClient.blockingConnect(CLIENT_CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS);
         }
      }).start();
      return true;
   }
}
