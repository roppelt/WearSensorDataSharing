package com.biankaroppelt.masterthesis;

import android.util.Log;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.WearableListenerService;

import static com.biankaroppelt.datalogger.SharedStrings.SEND_DATA_ACCELEROMETER;
import static com.biankaroppelt.datalogger.SharedStrings.SEND_DATA_ORIENTATION;

public class SensorReceiverService extends WearableListenerService {
   private static final String TAG = SensorReceiverService.class.getSimpleName();

   private RemoteSensorManager sensorManager;

   @Override
   public void onCreate() {
      super.onCreate();
      sensorManager = RemoteSensorManager.getInstance(this);
   }

   @Override
   public void onMessageReceived(MessageEvent messageEvent) {
      if (messageEvent.getPath()
            .startsWith(SEND_DATA_ACCELEROMETER)) {
         String data = new String(messageEvent.getData());
         sensorManager.addSensorData(data);
      } else if (messageEvent.getPath()
            .startsWith(SEND_DATA_ORIENTATION)) {
         String data = new String(messageEvent.getData());
         sensorManager.addSensorData(data);
      }
   }

   @Override
   public void onPeerConnected(Node peer) {
      super.onPeerConnected(peer);
      Log.d(TAG, "Connected: " + peer.getDisplayName() + " (" + peer.getId() + ")");
   }

   @Override
   public void onPeerDisconnected(Node peer) {
      super.onPeerDisconnected(peer);
      Log.d(TAG, "Disconnected: " + peer.getDisplayName() + " (" + peer.getId() + ")");
   }
}
