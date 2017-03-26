package com.biankaroppelt.masterthesis;

import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import com.biankaroppelt.masterthesis.data.SensorDataPoint;
import com.biankaroppelt.masterthesis.events.BusProvider;
import com.biankaroppelt.masterthesis.events.DataPointAddedEvent;
import com.biankaroppelt.masterthesis.events.NewSensorEvent;
import com.biankaroppelt.masterthesis.events.NoNodesAvailableEvent;
import com.biankaroppelt.masterthesis.events.SensorUpdatedEvent;
import com.squareup.otto.Subscribe;

import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.ArrayList;

public class NewMainStudyActivity extends AppCompatActivity {

   private RemoteSensorManager remoteSensorManager;

   private static final String TAG = NewMainStudyActivity.class.getSimpleName();

   private Button buttonDataStart;
   private Button buttonDataStop;
   private CoordinatorLayout coordinatorLayout;
   private ProgressBar loadingIndicator;

   private WebSocketClient mWebSocketClient;

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main_study);
      getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

      buttonDataStart = ((Button) findViewById(R.id.button_data_start));
      buttonDataStop = ((Button) findViewById(R.id.button_data_stop));
      coordinatorLayout = ((CoordinatorLayout) findViewById(R.id.coordinator_layout));
      loadingIndicator = ((ProgressBar) findViewById(R.id.loading_indicator));

      remoteSensorManager = RemoteSensorManager.getInstance(this);
      setupToolbar();
      setupListener();
   }

   private void setupToolbar() {
      Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
      setSupportActionBar(toolbar);
   }

   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
      //      getMenuInflater().inflate(R.menu.menu_mobile, menu);
      return false;
   }

   @Override
   public boolean onPrepareOptionsMenu(Menu menu) {
      return super.onPrepareOptionsMenu(menu);
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      return super.onOptionsItemSelected(item);
   }

   private void setupListener() {
      buttonDataStart.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View view) {
            startCollectingData();
         }
      });
      buttonDataStop.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View view) {
            stopCollectingData();
         }
      });
   }

   private void startCollectingData() {
      buttonDataStop.setVisibility(View.VISIBLE);
      buttonDataStart.setVisibility(View.GONE);
      loadingIndicator.setVisibility(View.GONE);
      remoteSensorManager.startMeasurementOrientationMainStudy();
   }

   private void stopCollectingData() {
      buttonDataStop.setVisibility(View.GONE);
      buttonDataStart.setVisibility(View.VISIBLE);
      loadingIndicator.setVisibility(View.GONE);
      remoteSensorManager.stopMeasurementOrientationMainStudy();
   }

   @Override
   protected void onResume() {
      super.onResume();
      BusProvider.getInstance()
            .register(this);
      if (mWebSocketClient == null || !mWebSocketClient.getReadyState()
            .equals(WebSocket.READYSTATE.OPEN)) {
         connectWebSocket();
      }
   }

   @Override
   protected void onPause() {
      super.onPause();
      BusProvider.getInstance()
            .unregister(this);
      stopCollectingData();
   }

   @Subscribe
   public void onNewSensorEvent(final NewSensorEvent event) {
   }

   @Subscribe
   public void onSensorUpdatedEvent(final SensorUpdatedEvent event) {
      System.out.println("onSensorUpdatedEvent");
      if (mWebSocketClient != null && mWebSocketClient.getReadyState()
            .equals(WebSocket.READYSTATE.OPEN)) {
         mWebSocketClient.send(buildSendString(event.getDataPointList()));
      } else {
         connectWebSocket();
      }
   }

   private String buildSendString(ArrayList<SensorDataPoint> dataPointList) {
      String string = "";
      //      for (int i = 0; i < dataPointList.size(); i++) {
      // Building the string only of the last dataPoint
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

   @Subscribe
   public void onDataPointAddedEvent(final DataPointAddedEvent event) {
   }

   @Subscribe
   public void onNoNodesAvailableEvent(final NoNodesAvailableEvent event) {
      Snackbar snackbar = Snackbar.make(coordinatorLayout, "No watch paired", Snackbar.LENGTH_LONG);
      snackbar.show();
      stopCollectingData();
   }

   private void connectWebSocket() {

      URI uri;
      uri = URI.create("ws://192.168.43.27:1234/");

      mWebSocketClient = new WebSocketClient(uri) {
         @Override
         public void onOpen(ServerHandshake serverHandshake) {
            System.out.println("websocket open");
         }

         @Override
         public void onMessage(String s) {
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
         public void onClose(int i, String s, boolean b) {
            android.util.Log.i("Websocket", "Closed " + s);
         }

         @Override
         public void onError(Exception e) {
            android.util.Log.i("Websocket", "Error " + e.getMessage());
            Snackbar snackbar =
                  Snackbar.make(coordinatorLayout, "Websocket error - retry", Snackbar.LENGTH_LONG);
            snackbar.show();
            connectWebSocket();
         }
      };
      mWebSocketClient.connect();
   }
}
