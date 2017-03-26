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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;

import com.biankaroppelt.masterthesis.data.SensorDataPoint;
import com.biankaroppelt.masterthesis.events.BusProvider;
import com.biankaroppelt.masterthesis.events.DataPointAddedEvent;
import com.biankaroppelt.masterthesis.events.NewSensorEvent;
import com.biankaroppelt.masterthesis.events.NoNodesAvailableEvent;
import com.biankaroppelt.masterthesis.events.OnMainStudyDataSentToServerEvent;
import com.biankaroppelt.masterthesis.events.OnPS2DataSentToServerEvent;
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

   //   private Button buttonDataSend;
   private Button buttonDataStart;
   private Button buttonDataStop;
   private TextInputEditText inputParticipant;
   private TextInputEditText inputHandSize;
   private CheckBox checkBoxHandedness;
   private CoordinatorLayout coordinatorLayout;
   private ProgressBar loadingIndicator;

   //   private ArrayList<SensorDataPoint> mItems;

   private WebSocketClient mWebSocketClient;

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main_study);
      getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

      //      buttonDataSend = ((Button) findViewById(R.id.button_data_send));
      buttonDataStart = ((Button) findViewById(R.id.button_data_start));
      buttonDataStop = ((Button) findViewById(R.id.button_data_stop));
      inputParticipant = ((TextInputEditText) findViewById(R.id.input_participant));
      inputHandSize = ((TextInputEditText) findViewById(R.id.input_hand_size));
      checkBoxHandedness = ((CheckBox) findViewById(R.id.input_handedness));
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
      //      buttonDataSend.setOnClickListener(new View.OnClickListener() {
      //         @Override
      //         public void onClick(View view) {
      //            sendCollectedData();
      //         }
      //      });
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
      remoteSensorManager.startMeasurementOrientationMainStudy();
   }

   private void stopCollectingData() {
      //      buttonDataSend.setVisibility(View.VISIBLE);
      buttonDataStop.setVisibility(View.GONE);
      buttonDataStart.setVisibility(View.VISIBLE);
      loadingIndicator.setVisibility(View.GONE);
      remoteSensorManager.stopMeasurementOrientationMainStudy();
   }

//   private void sendCollectedData() {
//      loadingIndicator.setVisibility(View.VISIBLE);
//      buttonDataSend.setVisibility(View.GONE);
//      buttonDataStart.setVisibility(View.GONE);
//      buttonDataStop.setVisibility(View.GONE);
//
//      sendAllData();
//   }

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
   public void onMainStudyDataSentToServerEvent(final OnMainStudyDataSentToServerEvent event) {
      boolean success = event.isSuccess();
      System.out.println(success);
      String snackbarString = "The dataset is stored in the database";
      if (!success) {
//         sendData(event.getRotationDimension(), event.getTarget(), event.getTaskSuccess());
         snackbarString = "unsuccessful (most of the time localtunnel didn't work) - Trying again";
      }
      Snackbar snackbar = Snackbar.make(coordinatorLayout, snackbarString, Snackbar.LENGTH_LONG);
      snackbar.show();

      buttonDataStart.setVisibility(View.VISIBLE);
      loadingIndicator.setVisibility(View.GONE);
      buttonDataStop.setVisibility(View.GONE);
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

   //   private void sendAllData() {
   //      for (int rotationDimension = 0; rotationDimension < mItems.length;
   //           rotationDimension++) {
   //      for (int selectedTargetId = 0; selectedTargetId < mItems[rotationDimension].length;
   // selectedTargetId++) {
   //            System.out.println("send " + selectedTargetId + " - " + rotationDimension + ": " +
   //                  mItems[rotationDimension][selectedTargetId]);
   //            sendData(rotationDimension, selectedTargetId, true);
   //         }
   //      }
   //   }
   //
   //   private void sendData(int rotationDimension, int selectedTargetId, boolean taskSuccess) {
   //      int participantId = Integer.parseInt(inputParticipant.getText()
   //            .toString());
   //      String handSizeString = inputHandSize.getText()
   //            .toString();
   //      boolean rightHanded = checkBoxHandedness.isChecked();
   //      String stringUrl = "http://192.168.43.27:8080/master/new_data_collection_pilot_study_2
   // .php";
   //      if (mItems[rotationDimension][selectedTargetId] != null) {
   //         new SendPilotStudy2DataToWebserverTask().execute(stringUrl, participantId,
   // selectedTargetId,
   //               rotationDimension, handSizeString, rightHanded, taskSuccess,
   //               mTargetInfo[rotationDimension][selectedTargetId].getTargetAngle(),
   //               mTargetInfo[rotationDimension][selectedTargetId].getMaxAngle(),
   //               mTargetInfo[rotationDimension][selectedTargetId].getVarianceInPercent(),
   //               mItems[rotationDimension][selectedTargetId],
   // mLogs[rotationDimension][selectedTargetId]);
   //      } else {
   //         System.out.println("NULL: " + selectedTargetId + " - " + rotationDimension);
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
