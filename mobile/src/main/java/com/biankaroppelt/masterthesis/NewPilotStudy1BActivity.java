package com.biankaroppelt.masterthesis;

import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;

import com.biankaroppelt.masterthesis.data.SensorDataPoint;
import com.biankaroppelt.masterthesis.events.BusProvider;
import com.biankaroppelt.masterthesis.events.NoNodesAvailableEvent;
import com.biankaroppelt.masterthesis.events.OnPS1BDataSentToServerEvent;
import com.biankaroppelt.masterthesis.events.SensorUpdatedEvent;
import com.squareup.otto.Subscribe;
import com.weiwangcn.betterspinner.library.material.MaterialBetterSpinner;

import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.ArrayList;

public class NewPilotStudy1BActivity extends AppCompatActivity {

   private final static String[] SPINNER_TAP_TYPE_LIST =
         { "Normal Taps", "Force Taps", "Pound out Taps" };
   private static final String TAG = NewPilotStudy1BActivity.class.getSimpleName();
   private Button buttonDataSend;
   private CoordinatorLayout coordinatorLayout;
   private TextInputEditText inputParticipant;
   private ArrayList<SensorDataPoint>[] items;
   private boolean[] itemsSaved;
   private ProgressBar loadingIndicator;
   private RemoteSensorManager remoteSensorManager;
   private int selectedTapTypeId;
   private MaterialBetterSpinner spinnerTapType;
   private WebSocketClient webSocketClient;

   @Subscribe
   public void onNoNodesAvailableEvent(final NoNodesAvailableEvent event) {
      Snackbar.make(coordinatorLayout, R.string.no_watch_paired, Snackbar.LENGTH_LONG)
            .show();
      stopCollectingData();
   }

   @Subscribe
   public void onOnDataSentToServerEvent(final OnPS1BDataSentToServerEvent event) {
      boolean success = event.isSuccess();
      if (success) {
         itemsSaved[event.getTapType()] = true;
         Snackbar.make(coordinatorLayout, R.string.successful_database_storage,
               Snackbar.LENGTH_LONG)
               .show();
      } else {
         sendData(event.getTapType());
         Snackbar.make(coordinatorLayout, R.string.unsuccessful_database_storage,
               Snackbar.LENGTH_LONG)
               .show();
      }
      boolean resetButtons = true;
      for (boolean itemSaved : itemsSaved) {
         if (!itemSaved) {
            resetButtons = false;
         }
      }
      if (resetButtons) {
         loadingIndicator.setVisibility(View.GONE);
         buttonDataSend.setEnabled(true);
      }
   }

   @Subscribe
   public void onSensorUpdatedEvent(final SensorUpdatedEvent event) {
      if (items[selectedTapTypeId] == null) {
         items[selectedTapTypeId] = new ArrayList<>();
      }
      items[selectedTapTypeId].addAll(event.getDataPointList());
      if (webSocketClient == null || !webSocketClient.getReadyState()
            .equals(WebSocket.READYSTATE.OPEN)) {
         connectWebSocket();
      }
      webSocketClient.send(buildSendString(event.getDataPointList()));
   }

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_pilot_study_1_b);
      buttonDataSend = ((Button) findViewById(R.id.button_data_send));
      spinnerTapType = ((MaterialBetterSpinner) findViewById(R.id.input_tap_type));
      inputParticipant = ((TextInputEditText) findViewById(R.id.input_participant));
      coordinatorLayout = ((CoordinatorLayout) findViewById(R.id.coordinator_layout));
      loadingIndicator = ((ProgressBar) findViewById(R.id.loading_indicator));

      remoteSensorManager = RemoteSensorManager.getInstance(this);
      items = new ArrayList[SPINNER_TAP_TYPE_LIST.length];
      itemsSaved = new boolean[SPINNER_TAP_TYPE_LIST.length];
      setupToolbar();
      setupListener();
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
            Log.d(TAG, "Websocket error " + e.getMessage());
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
                  if (message.equals("start_accelerometer_gyroscope")) {
                     startCollectingData();
                  } else if (message.equals("stop_accelerometer_gyroscope")) {
                     stopCollectingData();
                  } else if (message.startsWith("study_1b_tap_type")) {
                     String[] values = message.split(",");
                     int tapTypeFromProcessing = Integer.parseInt(values[1]);
                     spinnerTapType.setText(SPINNER_TAP_TYPE_LIST[tapTypeFromProcessing]);
                     selectedTapTypeId = tapTypeFromProcessing;
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

   private void sendAllData() {
      for (int tapType = 0; tapType < SPINNER_TAP_TYPE_LIST.length; tapType++) {
         sendData(tapType);
      }
   }

   private void sendCollectedData() {
      loadingIndicator.setVisibility(View.VISIBLE);
      buttonDataSend.setEnabled(false);
      sendAllData();
   }

   private void sendData(int tapType) {
      int participantId = Integer.parseInt(inputParticipant.getText()
            .toString());
      String stringUrl =
            getString(R.string.url_save_data, getString(R.string.save_data_file_pilot_1_b));

      final int dataPartitioningSize = items[tapType].size() / 500;
      for (int i = 0; i < dataPartitioningSize; i++) {
         final ArrayList<SensorDataPoint> tempList =
               new ArrayList<>(items[tapType].subList(i * 500, ((i + 1) * 500)));

         new SendPilotStudy1BDataToWebserverTask().execute(stringUrl, participantId, tapType,
               tempList);
      }
      final ArrayList<SensorDataPoint> tempList = new ArrayList<>(
            items[tapType].subList(dataPartitioningSize * 500, items[tapType].size()));
      new SendPilotStudy1BDataToWebserverTask().execute(stringUrl, participantId, tapType,
            tempList);
   }

   private void setupListener() {
      ArrayAdapter<String> arrayAdapterFinger =
            new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line,
                  SPINNER_TAP_TYPE_LIST);
      spinnerTapType.setAdapter(arrayAdapterFinger);
      spinnerTapType.setOnItemClickListener(new AdapterView.OnItemClickListener() {
         @Override
         public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            selectedTapTypeId = i;
         }
      });

      buttonDataSend.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View view) {
            sendCollectedData();
         }
      });
   }

   private void setupToolbar() {
      Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
      setSupportActionBar(toolbar);
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
   }

   private void startCollectingData() {
      loadingIndicator.setVisibility(View.VISIBLE);
      remoteSensorManager.startMeasurementAccelerometerPilotStudy1B();
   }

   private void stopCollectingData() {
      loadingIndicator.setVisibility(View.GONE);
      remoteSensorManager.stopMeasurementAccelerometerPilotStudy1B();
   }
}
