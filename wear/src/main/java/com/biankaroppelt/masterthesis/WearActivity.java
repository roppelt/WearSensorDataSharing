package com.biankaroppelt.masterthesis;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;

import com.biankaroppelt.masterthesis.events.StartMeasurementEvent;
import com.biankaroppelt.masterthesis.events.StopMeasurementEvent;
import com.squareup.otto.Subscribe;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

public class WearActivity extends WearableActivity {

   private TextView mTextView;

   private static final String TAG = WearActivity.class.getSimpleName();
   private PowerManager.WakeLock mWakeLock;

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
            mTextView = (TextView) stub.findViewById(R.id.text);
         }
      });

   }

   @Override
   public void onEnterAmbient(Bundle ambientDetails) {
      super.onEnterAmbient(ambientDetails);
      mTextView.setTextColor(Color.WHITE);
      mTextView.getPaint()
            .setAntiAlias(false);
   }

   @Override
   public void onExitAmbient() {
      super.onExitAmbient();
      mTextView.setTextColor(getResources().getColor(R.color.colorPrimary));
      mTextView.getPaint()
            .setAntiAlias(true);
   }

   @Override
   public void onUpdateAmbient() {
      super.onUpdateAmbient();

      // Update the content (once a minute)
   }
   @Subscribe
   public void onStartMeasurementEvent(final StartMeasurementEvent event) {

//      // TODO: move to activity
      mWakeLock = ((PowerManager)getSystemService(Context.POWER_SERVICE))
            .newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, getClass().getName());
      mWakeLock.acquire();
      // screen stays on in this section
      mWakeLock.release();
   }
   @Subscribe
   public void onStopMeasurementEvent(final StopMeasurementEvent event) {
      mWakeLock.release();
   }

}
