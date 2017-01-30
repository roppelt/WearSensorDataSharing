package com.biankaroppelt.masterthesis;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.SparseLongArray;

import com.biankaroppelt.datalogger.DataMapKeys;
import com.biankaroppelt.masterthesis.events.SensorEvent;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class DeviceClient {
   private static final String TAG = DeviceClient.class.getName();
   private static final int CLIENT_CONNECTION_TIMEOUT = 15000;

   public static DeviceClient instance;
   private List<Node> nodes;

   static DeviceClient getInstance(Context context) {
      if (instance == null) {
         instance = new DeviceClient(context.getApplicationContext());
      }

      return instance;
   }

   private Context context;
   private GoogleApiClient googleApiClient;
//   private ExecutorService executorService;
//   private ArrayList<SensorEvent> mItems;

   private SparseLongArray lastSensorData;

   private DeviceClient(Context context) {
      this.context = context;

      googleApiClient = new GoogleApiClient.Builder(context).addApi(Wearable.API)
            .build();

//      executorService = Executors.newCachedThreadPool();
      lastSensorData = new SparseLongArray();
//      mItems = new ArrayList<>();
   }

//   void addEventToList(SensorEvent event) {
//      mItems.add(event);
//   }
//
//   void sendSensorData() {
//      final int dataPartitioningSize = mItems.size() / 500;
//      for (int i = 0; i < dataPartitioningSize; i++) {
//         final ArrayList<SensorEvent> tempList =
//               new ArrayList<>(mItems.subList(i * 500, ((i + 1) * 500)));
//
//         final int finalI = i;
//         executorService.execute(new Runnable() {
//            @Override
//            public void run() {
//               try {
//                  Thread.sleep(finalI * 1000);
//                  sendSensorDataInBackground(tempList);
//               } catch (InterruptedException e) {
//                  e.printStackTrace();
//               }
//            }
//         });
//      }
//      final ArrayList<SensorEvent> tempList =
//            new ArrayList<>(mItems.subList(dataPartitioningSize * 500, mItems.size()));
//      executorService.execute(new Runnable() {
//         @Override
//         public void run() {
//            try {
//               Thread.sleep(dataPartitioningSize * 1000);
//               sendSensorDataInBackground(tempList);
//            } catch (InterruptedException e) {
//               e.printStackTrace();
//            }
//         }
//      });
//   }

   void sendSensorDataOrientation(final int index, final int sensorType, final int accuracy,
         final long timestamp, boolean absolute, final float[] values) {
      long t = System.currentTimeMillis();
      final String data =
            index + ";" + sensorType + ";" + accuracy + ";" + timestamp + ";" + absolute + ";" +
                  Arrays.toString(values);
      lastSensorData.put(sensorType, t);
      sendMessage("/sensorsOrientation/", data);
   }

   void sendSensorDataAccelerometerGyroscope(final int index, final int sensorType,
         final int accuracy, final long timestamp, boolean absolute, final float[] values) {
      long t = System.currentTimeMillis();
      final String data =
            index + ";" + sensorType + ";" + accuracy + ";" + timestamp + ";" + absolute + ";" +
                  Arrays.toString(values);
      lastSensorData.put(sensorType, t);
      sendMessage("/sensorsAccelerometerGyroscope/", data);
   }

   //   private void sendSensorDataAccelerometerGyroscopeInBackground(int index, int sensorType,
   //         int accuracy, long timestamp, float[] values) {
   //      //      PutDataMapRequest dataMap = PutDataMapRequest.create
   //      // ("/sensorsAccelerometerGyroscope/");
   //      //
   //      //      dataMap.getDataMap().putInt(DataMapKeys.INDEX, index);
   //      //      dataMap.getDataMap().putInt(DataMapKeys.SENSOR_TYPE, sensorType);
   //      //      dataMap.getDataMap()
   //      //            .putInt(DataMapKeys.ACCURACY, accuracy);
   //      //      dataMap.getDataMap()
   //      //            .putLong(DataMapKeys.TIMESTAMP, timestamp);
   //      //      dataMap.getDataMap()
   //      //            .putFloatArray(DataMapKeys.VALUES, values);
   //      //
   //      //      PutDataRequest putDataRequest = dataMap.asPutDataRequest();
   //      //      send(putDataRequest);
   //      String data = index + ";" + sensorType + ";" + accuracy + ";" + timestamp + ";" +
   //            Arrays.toString(values);
   //      System.out.println("SENDING DATA: " + data);
   //      sendMessage("/sensorsAccelerometerGyroscope/", data);
   //   }

//   private void sendSensorDataInBackground(ArrayList<SensorEvent> sensorData) {
//      PutDataMapRequest dataMap = PutDataMapRequest.create("/sensors/");
//
//      ArrayList<DataMap> list = new ArrayList<>();
//      for (SensorEvent event : sensorData) {
//         DataMap map = new DataMap();
//         map.putInt(DataMapKeys.SENSOR_TYPE, event.sensor.getType());
//         map.putInt(DataMapKeys.ACCURACY, event.accuracy);
//         map.putLong(DataMapKeys.TIMESTAMP, event.timestamp);
//         map.putFloatArray(DataMapKeys.VALUES, event.values);
//         list.add(map);
//      }
//      dataMap.getDataMap()
//            .putDataMapArrayList(DataMapKeys.LIST, list);
//
//      PutDataRequest putDataRequest = dataMap.asPutDataRequest();
//      send(putDataRequest);
//   }

   //   private void sendSensorDataOrientationInBackground(int sensorType, int accuracy, long
   // timestamp,
   //         float[] values) {
   //      PutDataMapRequest dataMap = PutDataMapRequest.create("/sensorsOrientation/");
   //
   //      dataMap.getDataMap()
   //            .putInt(DataMapKeys.SENSOR_TYPE, sensorType);
   //      dataMap.getDataMap()
   //            .putInt(DataMapKeys.ACCURACY, accuracy);
   //      dataMap.getDataMap()
   //            .putLong(DataMapKeys.TIMESTAMP, timestamp);
   //      dataMap.getDataMap()
   //            .putFloatArray(DataMapKeys.VALUES, values);
   //
   //      PutDataRequest putDataRequest = dataMap.asPutDataRequest();
   //      send(putDataRequest);
   //   }

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

   private void send(PutDataRequest putDataRequest) {
      if (validateConnection()) {
         Wearable.DataApi.putDataItem(googleApiClient, putDataRequest)
               .setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                  @Override
                  public void onResult(@NonNull DataApi.DataItemResult dataItemResult) {
                     Log.v(TAG, "Sending sensor data: " + dataItemResult.getStatus());
                  }
               });
      }
      System.gc();
   }

   private void sendMessage(final String path, final String data) {
      if (validateConnection()) {
         if (nodes == null || nodes.isEmpty()) {
            Log.e(TAG, "NO NODE");
            new Thread(new Runnable() {
               @Override
               public void run() {
                  googleApiClient.blockingConnect(CLIENT_CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS);
                  nodes = Wearable.NodeApi.getConnectedNodes(googleApiClient).await().getNodes();
                  sendMessageToNodes(path, data);
//                        .setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
//                           @Override
//                           public void onResult(
//                                 @NonNull NodeApi.GetConnectedNodesResult getConnectedNodesResult) {
//                              Log.e(TAG, "NODE SUCCESS");
//                              nodes = getConnectedNodesResult.getNodes();
//                              sendMessageToNodes(path, data);
//                           }
//                        });
               }
            }).start();
         } else {
            for (Node node : nodes) {
               Log.i(TAG, "add node " + node);
               sendMessageToNodes(path, data);
            }
         }
      } else {
         Log.w(TAG, "No connection possible");
      }
   }

   private void sendMessageToNodes(final String path, String data) {
      for (Node node : nodes) {
         Wearable.MessageApi.sendMessage(googleApiClient, node.getId(), path, data.getBytes())
               .setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
                  @Override
                  public void onResult(@NonNull MessageApi.SendMessageResult sendMessageResult) {
                     Log.d(TAG, "RESULT IN SENDING DATA BACK TO PHONE(" + path + "): " +
                           sendMessageResult.getStatus()
                                 .isSuccess());
                  }
               });
      }
   }
}
