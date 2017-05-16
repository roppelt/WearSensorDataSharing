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
import com.biankaroppelt.masterthesis.events.OnPS1ADataSentToServerEvent;
import com.biankaroppelt.masterthesis.events.SensorUpdatedEvent;
import com.squareup.otto.Subscribe;
import com.weiwangcn.betterspinner.library.material.MaterialBetterSpinner;

import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.ArrayList;

public class NewPilotStudy1AActivity extends AppCompatActivity {

   private static final String[] SPINNER_FINGER_LIST =
         { "Pinky", "Ring finger", "Middle finger", "Index finger", "Thumb" };
   private static final String[] SPINNER_ROTATION_DIMENSION_LIST =
         { "Lift up", "Lift down", "Roll left", "Roll right", "Rotate left", "Rotate right" };
   private static final String TAG = NewPilotStudy1AActivity.class.getSimpleName();
   private Button buttonDataSend;
   private CoordinatorLayout coordinatorLayout;
   private TextInputEditText inputParticipant;
   private ArrayList<SensorDataPoint>[][] items;
   private boolean[][] itemsSaved;
   private ProgressBar loadingIndicator;
   private RemoteSensorManager remoteSensorManager;
   private int selectedFingerId;
   private int selectedRotationDimensionId;
   private MaterialBetterSpinner spinnerDimension;
   private MaterialBetterSpinner spinnerFinger;
   private WebSocketClient webSocketClient;

   @Subscribe
   public void onNoNodesAvailableEvent(final NoNodesAvailableEvent event) {
      Snackbar.make(coordinatorLayout, R.string.no_watch_paired, Snackbar.LENGTH_LONG)
            .show();
      stopCollectingData();
   }

   @Subscribe
   public void onOnPS1ADataSentToServerEvent(final OnPS1ADataSentToServerEvent event) {
      boolean success = event.isSuccess();
      if (success) {
         itemsSaved[event.getFinger()][event.getRotationDimension()] = true;
         Snackbar.make(coordinatorLayout, R.string.successful_database_storage,
               Snackbar.LENGTH_LONG)
               .show();
      } else {
         sendData(event.getFinger(), event.getRotationDimension());
         Snackbar.make(coordinatorLayout, R.string.unsuccessful_database_storage,
               Snackbar.LENGTH_LONG)
               .show();
      }

      boolean resetButtons = true;
      for (boolean[] itemSavedFingerList : itemsSaved) {
         for (boolean itemSaved : itemSavedFingerList) {
            if (!itemSaved) {
               resetButtons = false;
            }
         }
      }
      if (resetButtons) {
         loadingIndicator.setVisibility(View.GONE);
         buttonDataSend.setEnabled(true);
      }
   }

   @Subscribe
   public void onSensorUpdatedEvent(final SensorUpdatedEvent event) {
      if (items[selectedFingerId][selectedRotationDimensionId] == null) {
         items[selectedFingerId][selectedRotationDimensionId] = new ArrayList<>();
      }
      items[selectedFingerId][selectedRotationDimensionId].addAll(event.getDataPointList());
      if (webSocketClient == null || !webSocketClient.getReadyState()
            .equals(WebSocket.READYSTATE.OPEN)) {
         connectWebSocket();
      }
      webSocketClient.send(buildSendString(event.getDataPointList()));
   }

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_pilot_study_1_a);
      buttonDataSend = ((Button) findViewById(R.id.button_data_send));
      spinnerFinger = ((MaterialBetterSpinner) findViewById(R.id.input_finger));
      spinnerDimension = ((MaterialBetterSpinner) findViewById(R.id.input_rotation_dimension));
      inputParticipant = ((TextInputEditText) findViewById(R.id.input_participant));
      coordinatorLayout = ((CoordinatorLayout) findViewById(R.id.coordinator_layout));
      loadingIndicator = ((ProgressBar) findViewById(R.id.loading_indicator));

      remoteSensorManager = RemoteSensorManager.getInstance(this);
      items = new ArrayList[SPINNER_FINGER_LIST.length][SPINNER_ROTATION_DIMENSION_LIST.length];
      itemsSaved = new boolean[SPINNER_FINGER_LIST.length][SPINNER_ROTATION_DIMENSION_LIST.length];
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
                  if (message.equals("start_orientation")) {
                     startCollectingData();
                  } else if (message.equals("stop_orientation")) {
                     stopCollectingData();
                  } else if (message.startsWith("study_1a_finger")) {
                     String[] values = message.split(",");
                     int fingerIdFromProcessing = Integer.parseInt(values[1]);
                     spinnerFinger.setText(SPINNER_FINGER_LIST[fingerIdFromProcessing]);
                     selectedFingerId = fingerIdFromProcessing;
                  } else if (message.startsWith("study_1a_rotation_dimension")) {
                     String[] values = message.split(",");
                     int rotationDimensionIdFromProcessing = Integer.parseInt(values[1]);
                     spinnerDimension.setText(
                           SPINNER_ROTATION_DIMENSION_LIST[rotationDimensionIdFromProcessing]);
                     selectedRotationDimensionId = rotationDimensionIdFromProcessing;
                  } else if (message.startsWith("study_1a_back")) {
                     String[] values = message.split(",");
                     int backFingerIdFromProcessing = Integer.parseInt(values[1]);
                     int backRotationDimensionIdFromProcessing = Integer.parseInt(values[2]);
                     items[backFingerIdFromProcessing][backRotationDimensionIdFromProcessing] =
                           new ArrayList<>();
                  } else if (message.startsWith("study_1a_participant_id")) {
                     String[] values = message.split(",");
                     inputParticipant.setText(values[1]);
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
      for (int finger = 0; finger < items.length; finger++) {
         for (int rotationDimension = 0; rotationDimension < items[finger].length;
              rotationDimension++) {
            sendData(finger, rotationDimension);
         }
      }
   }

   private void sendCollectedData() {
      loadingIndicator.setVisibility(View.VISIBLE);
      buttonDataSend.setEnabled(false);
      sendAllData();
   }

   private void sendData(int finger, int rotationDimension) {
      int participantId = Integer.parseInt(inputParticipant.getText()
            .toString());
      String stringUrl =
            getString(R.string.url_save_data, getString(R.string.save_data_file_pilot_1_a));
      if (items[finger][rotationDimension] != null) {
         new SendPilotStudy1ADataToWebserverTask().execute(stringUrl, participantId, finger,
               rotationDimension, items[finger][rotationDimension]);
      } else {
         Log.d(TAG, "Finger or rotation dimension null");
      }
   }

   private void setupListener() {
      ArrayAdapter<String> arrayAdapterFinger =
            new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line,
                  SPINNER_FINGER_LIST);
      spinnerFinger.setAdapter(arrayAdapterFinger);
      spinnerFinger.setOnItemClickListener(new AdapterView.OnItemClickListener() {
         @Override
         public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            selectedFingerId = i;
         }
      });

      ArrayAdapter<String> arrayAdapterRotationDimension =
            new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line,
                  SPINNER_ROTATION_DIMENSION_LIST);
      spinnerDimension.setAdapter(arrayAdapterRotationDimension);
      spinnerDimension.setOnItemClickListener(new AdapterView.OnItemClickListener() {
         @Override
         public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            selectedRotationDimensionId = i;
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
      remoteSensorManager.startMeasurementOrientationPilotStudy1A();
   }

   private void stopCollectingData() {
      loadingIndicator.setVisibility(View.GONE);
      remoteSensorManager.stopMeasurementOrientationPilotStudy1A();
   }
}
