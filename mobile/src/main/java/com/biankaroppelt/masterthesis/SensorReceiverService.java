package com.biankaroppelt.masterthesis;

import android.net.Uri;
import android.util.Log;

import com.biankaroppelt.datalogger.DataMapKeys;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.ArrayList;

public class SensorReceiverService extends WearableListenerService {
   private static final String TAG = "SensorReceiverService";

   private RemoteSensorManager sensorManager;

   @Override
   public void onCreate() {
      super.onCreate();

      sensorManager = RemoteSensorManager.getInstance(this);
   }

   @Override
   public void onPeerConnected(Node peer) {
      super.onPeerConnected(peer);

            Log.i(TAG, "Connected: " + peer.getDisplayName() + " (" + peer.getId() + ")");
   }

   @Override
   public void onPeerDisconnected(Node peer) {
      super.onPeerDisconnected(peer);

            Log.i(TAG, "Disconnected: " + peer.getDisplayName() + " (" + peer.getId() + ")");
   }

   @Override
   public void onDataChanged(DataEventBuffer dataEvents) {
      System.out.println("onDataChanged");
      //      Log.d(TAG, "onDataChanged()");
      ArrayList<DataMap> list = new ArrayList<>();

      for (DataEvent dataEvent : dataEvents) {
         if (dataEvent.getType() == DataEvent.TYPE_CHANGED) {
            DataItem dataItem = dataEvent.getDataItem();
            Uri uri = dataItem.getUri();
            String path = uri.getPath();

            if (path.startsWith("/sensors/")) {
               //               ArrayList<Object> element = new ArrayList<>();
               //               element.add(Integer.parseInt(uri.getLastPathSegment()));
               DataMapItem item = DataMapItem.fromDataItem(dataItem);
               DataMap map = item.getDataMap();
               if(map.containsKey("test")) {
                  System.out.println("test int: " + map.get("test"));
               }
               if (map.containsKey(DataMapKeys.LIST)) {
                  ArrayList<DataMap> dataMaps = map.get(DataMapKeys.LIST);
                  list.addAll(dataMaps);
               }
            }
         }
      }
      sensorManager.addSensorData(list);
   }
}
