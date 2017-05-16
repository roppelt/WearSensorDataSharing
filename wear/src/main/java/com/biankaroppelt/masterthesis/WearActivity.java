package com.biankaroppelt.masterthesis;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.content.ContextCompat;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.WatchViewStub;
import android.view.WindowManager;
import android.widget.TextView;

import com.biankaroppelt.masterthesis.events.StartMeasurementEvent;
import com.biankaroppelt.masterthesis.events.StopMeasurementEvent;
import com.squareup.otto.Subscribe;

public class WearActivity extends WearableActivity {

   private static final String TAG = WearActivity.class.getSimpleName();
   private TextView textView;
   private PowerManager.WakeLock wakeLock;

   @Override
   public void onEnterAmbient(Bundle ambientDetails) {
      super.onEnterAmbient(ambientDetails);
      textView.setTextColor(Color.WHITE);
      textView.getPaint()
            .setAntiAlias(false);
   }

   @Override
   public void onExitAmbient() {
      super.onExitAmbient();
      textView.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
      textView.getPaint()
            .setAntiAlias(true);
   }

   @Subscribe
   public void onStartMeasurementEvent(final StartMeasurementEvent event) {
      wakeLock = ((PowerManager) getSystemService(Context.POWER_SERVICE)).newWakeLock(
            PowerManager.SCREEN_BRIGHT_WAKE_LOCK, getClass().getName());
      wakeLock.acquire();
      wakeLock.release();
   }

   @Subscribe
   public void onStopMeasurementEvent(final StopMeasurementEvent event) {
      wakeLock.release();
   }

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setupUi();
      getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
   }

   private void setupUi() {
      setContentView(R.layout.activity_wear);
      setAmbientEnabled();
      final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
      stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
         @Override
         public void onLayoutInflated(WatchViewStub stub) {
            textView = (TextView) stub.findViewById(R.id.text);
         }
      });
   }
}
