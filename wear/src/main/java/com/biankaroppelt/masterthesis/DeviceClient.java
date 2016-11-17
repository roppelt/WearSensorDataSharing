package com.biankaroppelt.masterthesis;

import android.content.Context;
import android.hardware.SensorEvent;
import android.util.Log;
import android.util.SparseLongArray;

import com.biankaroppelt.datalogger.DataMapKeys;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataMap;
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

   public static DeviceClient getInstance(Context context) {
      if (instance == null) {
         instance = new DeviceClient(context.getApplicationContext());
      }

      return instance;
   }

   private Context context;
   private GoogleApiClient googleApiClient;
   private ExecutorService executorService;

   private SparseLongArray lastSensorData;

   private DeviceClient(Context context) {
      this.context = context;

      googleApiClient = new GoogleApiClient.Builder(context).addApi(Wearable.API)
            .build();

      executorService = Executors.newCachedThreadPool();
      lastSensorData = new SparseLongArray();
   }

   public void sendSensorData(final ArrayList<SensorEvent> sensorData) {

      System.out.println("sendSensorData");
      final int dataPartitioningSize = sensorData.size() / 500;
      for(int i = 0; i < dataPartitioningSize; i++) {
         final ArrayList<SensorEvent> tempList = new ArrayList<>(sensorData.subList(i*500, ((i+1)*500)));

         final int finalI = i;
         executorService.execute(new Runnable() {
            @Override
            public void run() {
               try {
                  Thread.sleep(finalI * 1000);
                  sendSensorDataInBackground(finalI, tempList);
               } catch (InterruptedException e) {
                  e.printStackTrace();
               }

            }
         });
      }
      final ArrayList<SensorEvent> tempList = new ArrayList<>(sensorData.subList(dataPartitioningSize*500, sensorData.size()));
      executorService.execute(new Runnable() {
         @Override
         public void run() {
            try {
               Thread.sleep(dataPartitioningSize * 1000);
               sendSensorDataInBackground(dataPartitioningSize, tempList);
            } catch (InterruptedException e) {
               e.printStackTrace();
            }
         }
      });
   }

   public void sendSensorData(final int sensorType, final int accuracy, final long timestamp,
         final float[] values) {
      long t = System.currentTimeMillis();
      //
      //      long lastTimestamp = lastSensorData.get(sensorType);
      //      long timeAgo = t - lastTimestamp;
      //
      //      if (lastTimestamp != 0) {
      //         if (filterId == sensorType && timeAgo < 100) {
      //            return;
      //         }
      //
      //         if (filterId != sensorType && timeAgo < 3000) {
      //            return;
      //         }
      //      }

      lastSensorData.put(sensorType, t);

      executorService.submit(new Runnable() {
         @Override
         public void run() {
            sendSensorDataInBackground(sensorType, accuracy, timestamp, values);
         }
      });
   }

   private void sendSensorDataInBackground(int test, ArrayList<SensorEvent> sensorData) {
      System.out.println("sendSensorDataInBackground");
      PutDataMapRequest dataMap = PutDataMapRequest.create("/sensors/" );

      ArrayList<DataMap> list = new ArrayList<>();
      for(SensorEvent event : sensorData) {
         DataMap map = new DataMap();
         map.putInt(DataMapKeys.SENSOR_TYPE, event.sensor.getType());
         map.putInt(DataMapKeys.ACCURACY, event.accuracy);
         map.putLong(DataMapKeys.TIMESTAMP, event.timestamp);
         map.putFloatArray(DataMapKeys.VALUES, event.values);
         list.add(map);
      }
      dataMap.getDataMap().putInt("test", test);
      System.out.println("test int wear: " + test);
      dataMap.getDataMap().putDataMapArrayList(DataMapKeys.LIST, list);

      PutDataRequest putDataRequest = dataMap.asPutDataRequest();
      send(putDataRequest);
   }

   private void sendSensorDataInBackground(int sensorType, int accuracy, long timestamp,
         float[] values) {
      PutDataMapRequest dataMap = PutDataMapRequest.create("/sensors/" + sensorType);

      dataMap.getDataMap()
            .putInt(DataMapKeys.ACCURACY, accuracy);
      dataMap.getDataMap()
            .putLong(DataMapKeys.TIMESTAMP, timestamp);
      dataMap.getDataMap()
            .putFloatArray(DataMapKeys.VALUES, values);

      PutDataRequest putDataRequest = dataMap.asPutDataRequest();
      send(putDataRequest);
   }

   private boolean validateConnection() {
      if (googleApiClient.isConnected()) {
         return true;
      }
      ConnectionResult result =
            googleApiClient.blockingConnect(CLIENT_CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS);

      return result.isSuccess();
   }

   private void send(PutDataRequest putDataRequest) {
      System.out.println("send");
      if (validateConnection()) {
         Wearable.DataApi.putDataItem(googleApiClient, putDataRequest)
               .setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                  @Override
                  public void onResult(DataApi.DataItemResult dataItemResult) {
                     Log.v(TAG, "Sending sensor data: " + dataItemResult.getStatus());
                  }
               });
      }
//      System.gc();
   }
}
