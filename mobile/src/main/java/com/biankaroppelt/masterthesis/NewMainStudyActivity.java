package com.biankaroppelt.masterthesis;

import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.biankaroppelt.masterthesis.data.SensorDataPoint;
import com.biankaroppelt.masterthesis.events.BusProvider;
import com.biankaroppelt.masterthesis.events.NoNodesAvailableEvent;
import com.biankaroppelt.masterthesis.events.SensorUpdatedEvent;
import com.squareup.otto.Subscribe;

import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.ArrayList;

public class NewMainStudyActivity extends AppCompatActivity {

   private static final String TAG = NewMainStudyActivity.class.getSimpleName();
   private CoordinatorLayout coordinatorLayout;
   private ProgressBar loadingIndicator;
   private RemoteSensorManager remoteSensorManager;
   private WebSocketClient webSocketClient;

   @Subscribe
   public void onNoNodesAvailableEvent(final NoNodesAvailableEvent event) {
      Snackbar.make(coordinatorLayout, R.string.no_watch_paired, Snackbar.LENGTH_LONG)
            .show();
      stopCollectingData();
   }

   @Subscribe
   public void onSensorUpdatedEvent(final SensorUpdatedEvent event) {
      if (webSocketClient != null && webSocketClient.getReadyState()
            .equals(WebSocket.READYSTATE.OPEN)) {
         webSocketClient.send(buildSendString(event.getDataPointList()));
      } else {
         connectWebSocket();
      }
   }

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main_study);
      getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
      coordinatorLayout = ((CoordinatorLayout) findViewById(R.id.coordinator_layout));
      loadingIndicator = ((ProgressBar) findViewById(R.id.loading_indicator));
      remoteSensorManager = RemoteSensorManager.getInstance(this);
      setupToolbar();
   }

   @Override
   protected void onPause() {
      super.onPause();
      BusProvider.getInstance()
            .unregister(this);
      stopCollectingData();
      webSocketClient.close();
   }

   @Override
   protected void onResume() {
      super.onResume();
      BusProvider.getInstance()
            .register(this);
      if (webSocketClient == null || !webSocketClient.getReadyState()
            .equals(WebSocket.READYSTATE.OPEN)) {
         connectWebSocket();
      }
   }

   private String buildSendString(ArrayList<SensorDataPoint> dataPointList) {
      String string = "";
      SensorDataPoint sensorDataPoint = dataPointList.get(dataPointList.size() - 1);
      if (sensorDataPoint.isAbsolute()) {
         if (dataPointList.size() <= 1 || dataPointList.get(dataPointList.size() - 2)
               .isAbsolute()) {
            return "";
         }
      }
      string += sensorDataPoint.getSensor()
            .getId() + ",";
      string += sensorDataPoint.getTimestamp() + ",";
      int valuesLength = Math.min(3, sensorDataPoint.getValues().length);
      for (int j = 0; j < valuesLength; j++) {
         string += sensorDataPoint.getValues()[j];
         if (j + 1 != valuesLength) {
            string += ",";
         }
      }
      return string;
   }

   private void connectWebSocket() {
      URI uri;
      uri = URI.create(getString(R.string.websocket_uri));
      webSocketClient = new WebSocketClient(uri) {
         @Override
         public void onClose(int i, String s, boolean b) {
            Log.d(TAG, "Websocket connection closed " + s);
         }

         @Override
         public void onError(Exception e) {
            Log.d(TAG, "Websocket error" + e.getMessage());
            Snackbar.make(coordinatorLayout, R.string.websocket_error, Snackbar.LENGTH_LONG)
                  .show();
            connectWebSocket();
         }

         @Override
         public void onMessage(String s) {
            Log.d(TAG, "Websocket message received");
            final String message = s.trim();
            runOnUiThread(new Runnable() {
               @Override
               public void run() {
                  if (message.equals("start_orientation")) {
                     startCollectingData();
                  } else if (message.startsWith("stop_orientation")) {
                     stopCollectingData();
                  }
               }
            });
         }

         @Override
         public void onOpen(ServerHandshake serverHandshake) {
            Log.d(TAG, "Websocket open");
         }
      };
      webSocketClient.connect();
   }

   private void setupToolbar() {
      Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
      setSupportActionBar(toolbar);
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
   }

   private void startCollectingData() {
      loadingIndicator.setVisibility(View.VISIBLE);
      remoteSensorManager.startMeasurementOrientationMainStudy();
   }

   private void stopCollectingData() {
      loadingIndicator.setVisibility(View.GONE);
      remoteSensorManager.stopMeasurementOrientationMainStudy();
   }
}
