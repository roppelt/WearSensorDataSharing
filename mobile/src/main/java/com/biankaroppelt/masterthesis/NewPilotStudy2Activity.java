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
import com.biankaroppelt.masterthesis.data.TargetInfo;
import com.biankaroppelt.masterthesis.events.BusProvider;
import com.biankaroppelt.masterthesis.events.NoNodesAvailableEvent;
import com.biankaroppelt.masterthesis.events.OnPS2DataSentToServerEvent;
import com.biankaroppelt.masterthesis.events.SensorUpdatedEvent;
import com.squareup.otto.Subscribe;
import com.weiwangcn.betterspinner.library.material.MaterialBetterSpinner;

import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.ArrayList;

public class NewPilotStudy2Activity extends AppCompatActivity {

   private final static String[] SPINNER_ROTATION_DIMENSION_LIST =
         { "0: Lift up", "1: Lift down", "2: Roll left", "3: Roll right", "4: Rotate left",
               "5: Rotate right" };
   private final static String[] SPINNER_TARGET_LIST = { "0", "1", "2", "3", "4" };
   private static final String TAG = NewPilotStudy2Activity.class.getSimpleName();
   private Button buttonDataSend;
   private CoordinatorLayout coordinatorLayout;
   private TextInputEditText inputParticipant;
   private ArrayList<SensorDataPoint>[][] items;
   private boolean[][] itemsSaved;
   private ProgressBar loadingIndicator;
   private RemoteSensorManager remoteSensorManager;
   private int selectedRotationDimensionId;
   private int selectedTargetId;
   private MaterialBetterSpinner spinnerDimension;
   private MaterialBetterSpinner spinnerTarget;
   private TargetInfo[][] targetInfos;
   private WebSocketClient webSocketClient;

   @Subscribe
   public void onNoNodesAvailableEvent(final NoNodesAvailableEvent event) {
      Snackbar.make(coordinatorLayout, R.string.no_watch_paired, Snackbar.LENGTH_LONG)
            .show();
      stopCollectingData();
   }

   @Subscribe
   public void onOnPS2DataSentToServerEvent(final OnPS2DataSentToServerEvent event) {
      boolean success = event.isSuccess();
      if (success) {
         itemsSaved[event.getRotationDimension()][event.getTarget()] = true;
         Snackbar.make(coordinatorLayout, R.string.successful_database_storage,
               Snackbar.LENGTH_LONG)
               .show();
      } else {
         sendData(event.getRotationDimension(), event.getTarget(), event.getTaskSuccess());
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
      if (items[selectedRotationDimensionId][selectedTargetId] == null) {
         items[selectedRotationDimensionId][selectedTargetId] = new ArrayList<>();
      }
      items[selectedRotationDimensionId][selectedTargetId].addAll(event.getDataPointList());
      if (webSocketClient == null || !webSocketClient.getReadyState()
            .equals(WebSocket.READYSTATE.OPEN)) {
         connectWebSocket();
      }
      webSocketClient.send(buildSendString(event.getDataPointList()));
   }

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_pilot_study_2);
      buttonDataSend = ((Button) findViewById(R.id.button_data_send));
      spinnerTarget = ((MaterialBetterSpinner) findViewById(R.id.input_target));
      spinnerDimension = ((MaterialBetterSpinner) findViewById(R.id.input_rotation_dimension));
      inputParticipant = ((TextInputEditText) findViewById(R.id.input_participant));
      coordinatorLayout = ((CoordinatorLayout) findViewById(R.id.coordinator_layout));
      loadingIndicator = ((ProgressBar) findViewById(R.id.loading_indicator));

      remoteSensorManager = RemoteSensorManager.getInstance(this);
      items = new ArrayList[SPINNER_ROTATION_DIMENSION_LIST.length][SPINNER_TARGET_LIST.length];
      itemsSaved = new boolean[SPINNER_ROTATION_DIMENSION_LIST.length][SPINNER_TARGET_LIST.length];
      targetInfos =
            new TargetInfo[SPINNER_ROTATION_DIMENSION_LIST.length][SPINNER_TARGET_LIST.length];
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
                  } else if (message.startsWith("stop_orientation")) {
                     String[] values = message.split(",");
                     int success = Integer.parseInt(values[1]);
                     stopCollectingData();
                     if (success == 0) {
                        sendData(selectedRotationDimensionId, selectedTargetId, false);
                        items[selectedRotationDimensionId][selectedTargetId] = new ArrayList<>();
                     }
                  } else if (message.startsWith("study_2_target_id")) {
                     String[] values = message.split(",");
                     int targetIdFromProcessing = Integer.parseInt(values[1]);
                     spinnerTarget.setText(SPINNER_TARGET_LIST[targetIdFromProcessing]);
                     selectedTargetId = targetIdFromProcessing;
                  } else if (message.startsWith("study_2_rotation_dimension")) {
                     String[] values = message.split(",");
                     int rotationDimensionIdFromProcessing = Integer.parseInt(values[1]);
                     spinnerDimension.setText(
                           SPINNER_ROTATION_DIMENSION_LIST[rotationDimensionIdFromProcessing]);
                     selectedRotationDimensionId = rotationDimensionIdFromProcessing;
                  } else if (message.startsWith("study_2_back")) {
                     String[] values = message.split(",");
                     int backRotationDimensionIdFromProcessing = Integer.parseInt(values[1]);
                     int backTargetIdFromProcessing = Integer.parseInt(values[2]);
                     items[backRotationDimensionIdFromProcessing][backTargetIdFromProcessing] =
                           new ArrayList<>();
                  } else if (message.startsWith("study_2_target_info")) {
                     String[] values = message.split(",");
                     float targetAngle = Float.parseFloat(values[1]);
                     float maxAngle = Float.parseFloat(values[2]);
                     float varianceInPercent = Float.parseFloat(values[3]);
                     targetInfos[selectedRotationDimensionId][selectedTargetId] =
                           new TargetInfo(targetAngle, maxAngle, varianceInPercent);
                  } else if (message.startsWith("study_2_participant_id")) {
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
      for (int rotationDimension = 0; rotationDimension < items.length; rotationDimension++) {
         for (int selectedTargetId = 0; selectedTargetId < items[rotationDimension].length;
              selectedTargetId++) {
            sendData(rotationDimension, selectedTargetId, true);
         }
      }
   }

   private void sendCollectedData() {
      buttonDataSend.setEnabled(false);
      loadingIndicator.setVisibility(View.VISIBLE);
      sendAllData();
   }

   private void sendData(int rotationDimension, int selectedTargetId, boolean taskSuccess) {
      int participantId = Integer.parseInt(inputParticipant.getText()
            .toString());
      String stringUrl =
            getString(R.string.url_save_data, getString(R.string.save_data_file_pilot_2));
      if (items[rotationDimension][selectedTargetId] != null) {
         new SendPilotStudy2DataToWebserverTask().execute(stringUrl, participantId,
               selectedTargetId, rotationDimension, taskSuccess,
               targetInfos[rotationDimension][selectedTargetId].getTargetAngle(),
               targetInfos[rotationDimension][selectedTargetId].getMaxAngle(),
               targetInfos[rotationDimension][selectedTargetId].getVarianceInPercent(),
               items[rotationDimension][selectedTargetId]);
      } else {
         Log.d(TAG, "Selected Target ID or rotation dimension null");
      }
   }

   private void setupListener() {
      ArrayAdapter<String> arrayAdapterTarget =
            new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line,
                  SPINNER_TARGET_LIST);
      spinnerTarget.setAdapter(arrayAdapterTarget);
      spinnerTarget.setOnItemClickListener(new AdapterView.OnItemClickListener() {
         @Override
         public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            selectedTargetId = i;
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
      remoteSensorManager.startMeasurementOrientationPilotStudy2();
   }

   private void stopCollectingData() {
      loadingIndicator.setVisibility(View.GONE);
      remoteSensorManager.stopMeasurementOrientationPilotStudy2();
   }
}
