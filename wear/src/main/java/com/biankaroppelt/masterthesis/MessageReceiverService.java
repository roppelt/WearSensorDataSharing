package com.biankaroppelt.masterthesis;

import android.content.Intent;
import android.util.Log;

import com.biankaroppelt.datalogger.SharedStrings;
import com.biankaroppelt.masterthesis.events.BusProvider;
import com.biankaroppelt.masterthesis.events.StartMeasurementEvent;
import com.biankaroppelt.masterthesis.events.StopMeasurementEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

public class MessageReceiverService extends WearableListenerService {
   private static final String TAG = MessageReceiverService.class.getSimpleName();

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
            .equals(SharedStrings.START_MEASUREMENT_ORIENTATION_PILOT_STUDY_1A)) {
         BusProvider.postOnMainThread(new StartMeasurementEvent());
         Intent serviceIntent = new Intent(this, SensorServiceOrientation.class);
         serviceIntent.putExtra(getString(R.string.extra_absolute_values), true);
         startService(serviceIntent);
      }

      if (messageEvent.getPath()
            .equals(SharedStrings.STOP_MEASUREMENT_ORIENTATION_PILOT_STUDY_1A)) {
         BusProvider.postOnMainThread(new StopMeasurementEvent());
         stopService(new Intent(this, SensorServiceOrientation.class));
      }

      if (messageEvent.getPath()
            .equals(SharedStrings.START_MEASUREMENT_ACCELEROMETER_PILOT_STUDY_1B)) {
         BusProvider.postOnMainThread(new StartMeasurementEvent());
         startService(new Intent(this, SensorServiceAccelerometer.class));
      }

      if (messageEvent.getPath()
            .equals(SharedStrings.STOP_MEASUREMENT_ACCELEROMETER_PILOT_STUDY_1B)) {
         BusProvider.postOnMainThread(new StopMeasurementEvent());
         stopService(new Intent(this, SensorServiceAccelerometer.class));
      }

      if (messageEvent.getPath()
            .equals(SharedStrings.START_MEASUREMENT_ORIENTATION_PILOT_STUDY_2)) {
         BusProvider.postOnMainThread(new StartMeasurementEvent());
         Intent serviceIntent = new Intent(this, SensorServiceOrientation.class);
         serviceIntent.putExtra(getString(R.string.extra_absolute_values), true);
         startService(serviceIntent);
      }

      if (messageEvent.getPath()
            .equals(SharedStrings.STOP_MEASUREMENT_ORIENTATION_PILOT_STUDY_2)) {
         BusProvider.postOnMainThread(new StopMeasurementEvent());
         stopService(new Intent(this, SensorServiceOrientation.class));
      }

      if (messageEvent.getPath()
            .equals(SharedStrings.START_MEASUREMENT_ORIENTATION_MAIN_STUDY)) {
         BusProvider.postOnMainThread(new StartMeasurementEvent());
         Intent serviceIntent = new Intent(this, SensorServiceOrientation.class);
         serviceIntent.putExtra(getString(R.string.extra_absolute_values), true);
         startService(serviceIntent);
      }

      if (messageEvent.getPath()
            .equals(SharedStrings.STOP_MEASUREMENT_ORIENTATION_MAIN_STUDY)) {
         BusProvider.postOnMainThread(new StopMeasurementEvent());
         stopService(new Intent(this, SensorServiceOrientation.class));
      }
   }
}
