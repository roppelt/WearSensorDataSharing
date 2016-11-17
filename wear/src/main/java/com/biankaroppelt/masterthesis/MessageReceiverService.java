package com.biankaroppelt.masterthesis;

import android.content.Intent;
import android.util.Log;

import com.biankaroppelt.datalogger.ClientPaths;
import com.biankaroppelt.masterthesis.events.BusProvider;
import com.biankaroppelt.masterthesis.events.StartMeasurementEvent;
import com.biankaroppelt.masterthesis.events.StopMeasurementEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

public class MessageReceiverService extends WearableListenerService {
   private static final String TAG = "MessageReceiverService";

   @Override
   public void onCreate() {
      super.onCreate();
   }

   @Override
   public void onDataChanged(DataEventBuffer dataEvents) {
      super.onDataChanged(dataEvents);
   }

   @Override
   public void onMessageReceived(MessageEvent messageEvent) {
      Log.d(TAG, "Received message: " + messageEvent.getPath());

      if (messageEvent.getPath()
            .equals(ClientPaths.START_MEASUREMENT)) {
//         BusProvider.postOnMainThread(new StartMeasurementEvent());
         startService(new Intent(this, SensorService.class));
      }

      if (messageEvent.getPath()
            .equals(ClientPaths.STOP_MEASUREMENT)) {
         BusProvider.postOnMainThread(new StopMeasurementEvent());
         stopService(new Intent(this, SensorService.class));
      }
   }
}
