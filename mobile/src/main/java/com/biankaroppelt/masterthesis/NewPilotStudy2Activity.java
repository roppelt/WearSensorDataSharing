package com.biankaroppelt.masterthesis;

import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;

import com.biankaroppelt.masterthesis.data.Log;
import com.biankaroppelt.masterthesis.data.SensorDataPoint;
import com.biankaroppelt.masterthesis.data.TargetInfo;
import com.biankaroppelt.masterthesis.events.BusProvider;
import com.biankaroppelt.masterthesis.events.DataPointAddedEvent;
import com.biankaroppelt.masterthesis.events.NewSensorEvent;
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
import java.util.Arrays;

public class NewPilotStudy2Activity extends AppCompatActivity {

   private RemoteSensorManager remoteSensorManager;

   private static final String TAG = NewPilotStudy2Activity.class.getSimpleName();

   //   private Button startCollectingDataButton;
   //   private Button stopCollectingDataButton;
   private MaterialBetterSpinner spinnerTarget;
   private MaterialBetterSpinner spinnerDimension;
   private Button buttonDataSend;
   private Button buttonDataStart;
   private Button buttonDataStop;
   private TextInputEditText inputParticipant;
   private TextInputEditText inputHandSize;
   private CheckBox checkBoxHandedness;
   private CoordinatorLayout coordinatorLayout;
   private ProgressBar loadingIndicator;

   //   private ArrayList<SensorDataPoint> mItems;

   private WebSocketClient mWebSocketClient;
   private int selectedRotationDimensionId;
   private int selectedTargetId;

   private ArrayList<SensorDataPoint>[][] mItems;
   private ArrayList<Log>[][] mLogs;
   private TargetInfo[][] mTargetInfo;
   private boolean[][] mItemsSaved;

   String[] SPINNER_TARGET_LIST = { "0", "1", "2", "3", "4" };
   String[] SPINNER_ROTATION_DIMENSION_LIST =
         { "0: Lift up", "1: Lift down", "2: Roll left", "3: Roll right", "4: Rotate left",
               "5: Rotate right" };

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_pilot_study_2);
      buttonDataSend = ((Button) findViewById(R.id.button_data_send));
      buttonDataStart = ((Button) findViewById(R.id.button_data_start));
      buttonDataStop = ((Button) findViewById(R.id.button_data_stop));
      spinnerTarget = ((MaterialBetterSpinner) findViewById(R.id.input_target));
      spinnerDimension = ((MaterialBetterSpinner) findViewById(R.id.input_rotation_dimension));
      inputParticipant = ((TextInputEditText) findViewById(R.id.input_participant));
      inputHandSize = ((TextInputEditText) findViewById(R.id.input_hand_size));
      checkBoxHandedness = ((CheckBox) findViewById(R.id.input_handedness));
      coordinatorLayout = ((CoordinatorLayout) findViewById(R.id.coordinator_layout));
      loadingIndicator = ((ProgressBar) findViewById(R.id.loading_indicator));

      remoteSensorManager = RemoteSensorManager.getInstance(this);
      mItems = new ArrayList[SPINNER_ROTATION_DIMENSION_LIST.length][SPINNER_TARGET_LIST.length];
      mItemsSaved = new boolean[SPINNER_ROTATION_DIMENSION_LIST.length][SPINNER_TARGET_LIST.length];
      mLogs = new ArrayList[SPINNER_ROTATION_DIMENSION_LIST.length][SPINNER_TARGET_LIST.length];
      mTargetInfo =
            new TargetInfo[SPINNER_ROTATION_DIMENSION_LIST.length][SPINNER_TARGET_LIST.length];
      //      mItems = new ArrayList<>();
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
      //      mItems = new ArrayList<>();
      buttonDataStop.setVisibility(View.VISIBLE);
      buttonDataStart.setVisibility(View.GONE);
      //      buttonDataSend.setVisibility(View.GONE);
      loadingIndicator.setVisibility(View.GONE);
      remoteSensorManager.startMeasurementOrientationPilotStudy2();
   }

   private void stopCollectingData() {
      //      buttonDataSend.setVisibility(View.VISIBLE);
      buttonDataStop.setVisibility(View.GONE);
      buttonDataStart.setVisibility(View.VISIBLE);
      loadingIndicator.setVisibility(View.GONE);
      remoteSensorManager.stopMeasurementOrientationPilotStudy2();
   }

   private void sendCollectedData() {
      loadingIndicator.setVisibility(View.VISIBLE);
      //      buttonDataSend.setVisibility(View.GONE);
      buttonDataStart.setVisibility(View.GONE);
      buttonDataStop.setVisibility(View.GONE);

      sendAllData();
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
      if (mItems[selectedRotationDimensionId][selectedTargetId] == null) {
         mItems[selectedRotationDimensionId][selectedTargetId] = new ArrayList<>();
      }
      System.out.println("onSensorUpdatedEvent " + selectedRotationDimensionId + "-" + selectedTargetId+ "    " + event.getDataPointList().size() + ": " + mItems[selectedRotationDimensionId][selectedTargetId].size());

      mItems[selectedRotationDimensionId][selectedTargetId].addAll(event.getDataPointList());
      System.out.println("afterSensorUpdatedEvent " + selectedRotationDimensionId + "-" + selectedTargetId+ "    " + event.getDataPointList().size() + ": " + mItems[selectedRotationDimensionId][selectedTargetId].size());
      boolean enableSendButton = true;
      for (int rotationDimension = 0; rotationDimension < mItems.length; rotationDimension++) {
         for (int target = 0; target < mItems[rotationDimension].length; target++) {
            if (mItems[rotationDimension][target] != null &&
                  mItems[rotationDimension][target].size() > 0) {
               System.out.println("new " + rotationDimension + " - " + target + ": " +
                     mItems[rotationDimension][target].size());
            } else {
               enableSendButton = false;
               System.out.println("ENABLED FALSE: " + rotationDimension + " - " + target);
            }
         }
      }
      if (enableSendButton) {
         //         buttonDataSend.setEnabled(true);
      }
      //      ArrayList<SensorDataPoint> mItemsTemp;
      //      mItems.addAll(event.getDataPointList());
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
      //         if (i + 1 != dataPointList.size()) {
      //            string += ";";
      //         }
      //      }
      return string;
   }

   @Subscribe
   public void onOnPS2DataSentToServerEvent(final OnPS2DataSentToServerEvent event) {
      boolean success = event.isSuccess();
      System.out.println(success);
      String snackbarString = "The dataset is stored in the database";
      if (success) {
         mItemsSaved[event.getRotationDimension()][event.getTarget()] = true;
      } else {
         sendData(event.getRotationDimension(), event.getTarget(), event.getTaskSuccess());
         snackbarString = "unsuccessful (most of the time localtunnel didn't work) - Trying again";
      }
      Snackbar snackbar = Snackbar.make(coordinatorLayout, snackbarString, Snackbar.LENGTH_LONG);
      snackbar.show();

      boolean resetButtons = true;
      for (int i = 0; i < mItemsSaved.length; i++) {
         for (int j = 0; j < mItemsSaved[i].length; j++) {
            if (!mItemsSaved[i][j]) {
               resetButtons = false;
            }
         }
      }
      if (resetButtons) {
         buttonDataStart.setVisibility(View.VISIBLE);
         loadingIndicator.setVisibility(View.GONE);
         buttonDataStop.setVisibility(View.GONE);
      }
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

   private void sendAllData() {
      for (int rotationDimension = 0; rotationDimension < mItems.length;
           rotationDimension++) {
      for (int selectedTargetId = 0; selectedTargetId < mItems[rotationDimension].length; selectedTargetId++) {
            System.out.println("send " + selectedTargetId + " - " + rotationDimension + ": " +
                  mItems[rotationDimension][selectedTargetId]);
            sendData(rotationDimension, selectedTargetId, true);
         }
      }
   }

   private void sendData(int rotationDimension, int selectedTargetId, boolean taskSuccess) {
      int participantId = Integer.parseInt(inputParticipant.getText()
            .toString());
      String handSizeString = inputHandSize.getText()
            .toString();
      boolean rightHanded = checkBoxHandedness.isChecked();
      String stringUrl = "http://192.168.43.27:8080/master/new_data_collection_pilot_study_2.php";
      if (mItems[rotationDimension][selectedTargetId] != null) {
         new SendPilotStudy2DataToWebserverTask().execute(stringUrl, participantId, selectedTargetId,
               rotationDimension, handSizeString, rightHanded, taskSuccess,
               mTargetInfo[rotationDimension][selectedTargetId].getTargetAngle(),
               mTargetInfo[rotationDimension][selectedTargetId].getMaxAngle(),
               mTargetInfo[rotationDimension][selectedTargetId].getVarianceInPercent(),
               mItems[rotationDimension][selectedTargetId], mLogs[rotationDimension][selectedTargetId]);
      } else {
         System.out.println("NULL: " + selectedTargetId + " - " + rotationDimension);
      }
   }

   //   private void sendData() {
   //      long diffInMs = (mItems.get(mItems.size() - 1)
   //            .getTimestamp() - mItems.get(0)
   //            .getTimestamp());
   //      if (diffInMs > 0) {
   //         System.out.println("Count sensor items: " + mItems.size() + " - Sample rate: " +
   //               (mItems.size() * 1000.0 / diffInMs));
   //      }

   //      ConnectivityManager connMgr =
   //            (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
   //      NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
   //      if (networkInfo != null && networkInfo.isConnected()) {

   //         final int dataPartitioningSize = mItems.size() / 500;
   //         for(int i = 0; i < dataPartitioningSize; i++) {
   //            final ArrayList<SensorDataPoint> tempList =
   //                  new ArrayList<>(mItems.subList(i * 500, ((i + 1) * 500)));

   //         }
   //         final ArrayList<SensorDataPoint> tempList = new ArrayList<>(mItems.subList
   // (dataPartitioningSize*500, mItems.size()));
   //         new SendDataToWebserverTask().execute(stringUrl, dataSetTitle, tempList);

   //      } else {
   //         // display error
   //         Snackbar snackbar = Snackbar.make(coordinatorLayout, "No network connection
   // available.",
   //               Snackbar.LENGTH_LONG);
   //         snackbar.show();
   //      }
   //   }

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
                     String[] values = message.split(",");
                     int success = Integer.parseInt(values[1]);
                     stopCollectingData();
                     if (success == 0) {
                        sendData(selectedRotationDimensionId, selectedTargetId, false);
                        mItems[selectedRotationDimensionId][selectedTargetId] = new ArrayList<>();
                        mLogs[selectedRotationDimensionId][selectedTargetId] = new ArrayList<>();
                     }
                  } else if (message.startsWith("study_2_target_id")) {
                     String[] values = message.split(",");
                     int targetIdFromProcessing = Integer.parseInt(values[1]);
                     System.out.println("TargetId: " + targetIdFromProcessing);
                     spinnerTarget.setText(SPINNER_TARGET_LIST[targetIdFromProcessing]);
                     selectedTargetId = targetIdFromProcessing;
                     System.out.println("FING: selectedTargetId: " + selectedTargetId +
                           " - selectedRotationDimensionId: " + selectedRotationDimensionId);
                  } else if (message.startsWith("study_2_rotation_dimension")) {
                     String[] values = message.split(",");
                     int rotationDimensionIdFromProcessing = Integer.parseInt(values[1]);
                     System.out.println(
                           "RotationDimensionId: " + rotationDimensionIdFromProcessing);
                     spinnerDimension.setText(
                           SPINNER_ROTATION_DIMENSION_LIST[rotationDimensionIdFromProcessing]);
                     selectedRotationDimensionId = rotationDimensionIdFromProcessing;
                     System.out.println("RT: selectedTargetId: " + selectedTargetId +
                           " - selectedRotationDimensionId: " + selectedRotationDimensionId);
                  } else if (message.startsWith("study_2_back")) {
                     String[] values = message.split(",");
                     int backRotationDimensionIdFromProcessing = Integer.parseInt(values[1]);
                     int backTargetIdFromProcessing = Integer.parseInt(values[2]);
                     mItems[backRotationDimensionIdFromProcessing][backTargetIdFromProcessing] =
                           new ArrayList<>();
                     mLogs[backRotationDimensionIdFromProcessing][backTargetIdFromProcessing] =
                           new ArrayList<>();

                     System.out.println("BACK: selectedTargetId: " + selectedTargetId +
                           " - selectedRotationDimensionId: " + selectedRotationDimensionId);

                     System.out.println("BACK: backtargetid: " + backTargetIdFromProcessing +
                           " - backrotationdimensionid: " + backRotationDimensionIdFromProcessing);
                  } else if (message.startsWith("study_2_target_info")) {
                     String[] values = message.split(",");
                     float targetAngle = Float.parseFloat(values[1]);
                     float maxAngle = Float.parseFloat(values[2]);
                     float varianceInPercent = Float.parseFloat(values[3]);
                     mTargetInfo[selectedRotationDimensionId][selectedTargetId] =
                           new TargetInfo(targetAngle, maxAngle, varianceInPercent);
                     System.out.println("TargetInfo: " + targetAngle + " - " + maxAngle + " - " +
                           varianceInPercent);
                  } else if (message.startsWith("study_2_log")) {
                     String[] values = message.split(",");
                     long timestamp = Long.parseLong(values[1]);
                     int logType = Integer.parseInt(values[2]);
                     if (mLogs[selectedRotationDimensionId][selectedTargetId] == null) {
                        mLogs[selectedRotationDimensionId][selectedTargetId] = new ArrayList<>();
                     }
                     mLogs[selectedRotationDimensionId][selectedTargetId].add(
                           new Log(timestamp, logType));
                     System.out.println("LOG: " + timestamp + " - " + logType);
                  } else if (message.startsWith("study_2_participant_id")) {
                     String[] values = message.split(",");
                     inputParticipant.setText(values[1]);
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