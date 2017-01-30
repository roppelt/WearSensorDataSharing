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

//      if (messageEvent.getPath()
//            .equals(ClientPaths.START_MEASUREMENT)) {
//         BusProvider.postOnMainThread(new StartMeasurementEvent());
//         startService(new Intent(this, SensorService.class));
//      }
//
//      if (messageEvent.getPath()
//            .equals(ClientPaths.STOP_MEASUREMENT)) {
//         BusProvider.postOnMainThread(new StopMeasurementEvent());
//         stopService(new Intent(this, SensorService.class));
//      }

      if (messageEvent.getPath()
            .equals(ClientPaths.START_MEASUREMENT_ORIENTATION)) {
         BusProvider.postOnMainThread(new StartMeasurementEvent());
         Intent serviceIntent = new Intent(this, SensorServiceOrientation.class);
         serviceIntent.putExtra("absolute", false);
         startService(serviceIntent);
      }

      if (messageEvent.getPath()
            .equals(ClientPaths.STOP_MEASUREMENT_ORIENTATION)) {
         BusProvider.postOnMainThread(new StopMeasurementEvent());
         stopService(new Intent(this, SensorServiceOrientation.class));
      }

      if (messageEvent.getPath()
            .equals(ClientPaths.START_MEASUREMENT_ORIENTATION_PILOT_STUDY_1A)) {
         BusProvider.postOnMainThread(new StartMeasurementEvent());
         Intent serviceIntent = new Intent(this, SensorServiceOrientation.class);
         serviceIntent.putExtra("absolute", true);
         startService(serviceIntent);
      }

      if (messageEvent.getPath()
            .equals(ClientPaths.START_MEASUREMENT_ORIENTATION_PILOT_STUDY_2)) {
         System.out.println("START 2");
         BusProvider.postOnMainThread(new StartMeasurementEvent());
         Intent serviceIntent = new Intent(this, SensorServiceOrientation.class);
         serviceIntent.putExtra("absolute", true);
         startService(serviceIntent);
      }

      if (messageEvent.getPath()
            .equals(ClientPaths.STOP_MEASUREMENT_ORIENTATION_PILOT_STUDY_1A)) {
         BusProvider.postOnMainThread(new StopMeasurementEvent());
         stopService(new Intent(this, SensorServiceOrientation.class));
      }

      if (messageEvent.getPath()
            .equals(ClientPaths.STOP_MEASUREMENT_ORIENTATION_PILOT_STUDY_2)) {
         System.out.println("STOP 2");
         BusProvider.postOnMainThread(new StopMeasurementEvent());
         stopService(new Intent(this, SensorServiceOrientation.class));
      }

      if (messageEvent.getPath()
            .equals(ClientPaths.START_MEASUREMENT_ACCELEROMETER_GYROSCOPE)) {
         BusProvider.postOnMainThread(new StartMeasurementEvent());
         startService(new Intent(this, SensorServiceAccelerometerGyroscope.class));
      }

      if (messageEvent.getPath()
            .equals(ClientPaths.STOP_MEASUREMENT_ACCELEROMETER_GYROSCOPE)) {
         BusProvider.postOnMainThread(new StopMeasurementEvent());
         stopService(new Intent(this, SensorServiceAccelerometerGyroscope.class));
      }
   }
}
